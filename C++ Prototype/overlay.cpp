#include <sys/time.h>
#include <iostream>
#include <stdio.h>
#include <getopt.h>
#include <vector>
#include <map>

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include "apriltags/CameraUtil.h"
#include "apriltags/TagDetector.h"

enum Tile {
  ONE = 1,
  TWO = 2,
  THREE = 3,
  FOUR = 4,
  FIVE = 5,
  SIX = 6,
  SEVEN = 7,
  EIGHT = 8,
  BLANK = 0,
  MINE = 9,
  FLAG = 10,
};

std::map<Tile, cv::Mat> tile_images;
cv::Mat tile_mask;
cv::Mat tile_points;

// We need to map the id to an offset from 0, 0. The origin, 0, 0, will be found
// at the top left of the board. Numbers will increment across the board (across
// a row).
std::vector<cv::Point3d> tile_offsets;
// All of the corners of the different tiles in the image, in the order:
//       1 * * * * * 2
//       * * * * * * *
//       * * * * * * *
//       4 * * * * * 3
std::vector<cv::Point3d> corners_all;

inline double IN2M(double x) {
  return .0254 * x;
}

void load_tiles() {
  tile_images[ONE] = cv::imread("images/1.png", CV_LOAD_IMAGE_COLOR);
  tile_images[TWO] = cv::imread("images/2.png", CV_LOAD_IMAGE_COLOR);
  tile_images[THREE] = cv::imread("images/3.png", CV_LOAD_IMAGE_COLOR);
  tile_images[FOUR] = cv::imread("images/4.png", CV_LOAD_IMAGE_COLOR);
  tile_images[FIVE] = cv::imread("images/5.png", CV_LOAD_IMAGE_COLOR);
  tile_images[SIX] = cv::imread("images/6.png", CV_LOAD_IMAGE_COLOR);
  tile_images[SEVEN] = cv::imread("images/7.png", CV_LOAD_IMAGE_COLOR);
  tile_images[EIGHT] = cv::imread("images/8.png", CV_LOAD_IMAGE_COLOR);
  tile_images[BLANK] = cv::imread("images/blank.png", CV_LOAD_IMAGE_COLOR);
  tile_images[MINE] = cv::imread("images/x.png", CV_LOAD_IMAGE_COLOR);
  tile_images[FLAG] = cv::imread("images/f.png", CV_LOAD_IMAGE_COLOR);

  const size_t tile_width = tile_images[BLANK].cols;
  const size_t tile_height = tile_images[BLANK].rows;

  tile_points = (cv::Mat_<double>(4, 2) <<
      0.0f, 0.0f,
      tile_width, 0.0f,
      tile_width, tile_height,
      0.0f, tile_height
  );

  // All the tiles have the same mask, so it can be generated once.
  tile_mask = cv::Mat(tile_images[BLANK].size(), CV_8U, cv::Scalar(255));

  const double row_width = 16; // inches
  const double col_height = 16; // inches
  auto top_left = cv::Point3d(IN2M(-col_height / 2), IN2M(row_width/ 2), 0);
  auto top_right = cv::Point3d(IN2M(col_height/ 2), IN2M(row_width/ 2), 0);
  auto bot_right = cv::Point3d(IN2M(col_height/ 2), IN2M(-row_width/ 2), 0);
  auto bot_left = cv::Point3d(IN2M(-col_height/ 2), IN2M(-row_width/ 2), 0);

  for (int row = 0; row < 1; ++row) {
    for (int col = 0; col < 2; ++col) {
      // Find the center of each tile, in a global coordinate space. The center
      // of the tile is treated as (0, 0) for the local coordinate frame, so all
      // of the points need to be translated to be relative to that origin,
      // which is why the center is being added to tile_offsets.
      auto center = cv::Point3d(IN2M(col * col_height + col_height / 2),
          IN2M(row * row_width + row_width / 2), IN2M(0));
      tile_offsets.push_back(center);
      std::cout << "Center: " << center << std::endl;

      // We push the global corner points in the order specified above onto
      // corners_all to get points to project later.
      corners_all.push_back(center + top_left);
      corners_all.push_back(center + top_right);
      corners_all.push_back(center + bot_right);
      corners_all.push_back(center + bot_left);
    }
  }
}

void processs_detections(const TagDetectionArray& detections,
    const cv::Point2d opticalCenter, cv::Mat* frame) {
  static double s = .1540; // tag size in meters
  static double ss = 1.5 * s; // half tag size in meters
  static double f = 500;

  cv::Mat K = (cv::Mat_<double>(3, 3) <<
    f, 0, opticalCenter.x,
    0, f, opticalCenter.y,
    0, 0, 1
  );

  // Shouldn't usually be static, but it's set to zeros always.
  static cv::Mat_<double> distCoeffs = cv::Mat_<double>::zeros(4,1);
  std::vector<std::vector<cv::Point2d>> all_frame_points;

  for (const auto& detection : detections) {
    if (!detection.good) continue;
    // First, we get the homography from the april tag.
    cv::Mat r, t; // Rotation and translation matrices
    CameraUtil::homographyToPoseCV(f, f, s, detection.homography, r, t);

    // The offset to apply to the entire set of corner points. Essentially,
    // we're shifting the grid to be centered on the center of the current april
    // tag.
    auto offset = tile_offsets[detection.id];
    auto corners(corners_all);
    for (auto& corner : corners) {
      corner -= offset;
    }

    // Project the corners of the ENTIRE TILE (16" x 16") into frame points,
    // which are image coordinates. These image coordinates then need to get
    // saved and eventually averaged.
    std::vector<cv::Point2d> frame_points;
    cv::projectPoints(corners, r, t, K, distCoeffs, frame_points);
    all_frame_points.push_back(frame_points);
  }

  // Now we have a vector of vectors, containing a bunch of theoretical frame
  // points. They need to all be averaged together and distilled into a single
  // vector.
  std::vector<cv::Point2d> frame_points;
  for (int i = 0; i < corners_all.size(); ++i) {
    cv::Point2d current_point(0, 0);

    for (int j = 0; j < all_frame_points.size(); ++j) {
      current_point += all_frame_points[j][i];
    }

    //Push back the average of all of the points.
    frame_points.push_back(current_point / (double)all_frame_points.size());
  }

  for (int i = 0; i < 2; ++i) {
    std::vector<cv::Point2d>::const_iterator begin = (
        frame_points.begin() + i * 4);
    std::vector<cv::Point2d>::const_iterator end = (
        frame_points.begin() + (i + 1) * 4);
    std::vector<cv::Point2d> points(begin, end);
    
    // Find a mapping from the full tile to the image coordindates that the
    // warped image is supposed to be at.
    cv::Mat H = cv::findHomography(tile_points, points, 0);
    
    // Finally, wth the warp calculated above, warp the actual image.
    cv::Mat tile_warped;
    cv::Mat mask_warped;
    cv::warpPerspective(tile_images[Tile(i % 8)], tile_warped, H,
        frame->size());
    cv::warpPerspective(tile_mask, mask_warped, H, frame->size());
    tile_warped.copyTo(*frame, mask_warped);
  }
}

int main(int argc, char** argv) {
  load_tiles();

  cv::Mat frame;
  cv::VideoCapture camera(0);
  const std::string WINDOW = "Look at me!";
  cv::namedWindow(WINDOW, cv::WINDOW_AUTOSIZE);

  TagDetectorParams params;
  params.segDecimate = true;
  params.segSigma = .8;
  params.thetaThresh = 100.0;
  params.magThresh = 1200.0;
  params.adaptiveThresholdValue = 5;
  params.adaptiveThresholdRadius = 9;
  params.refineBad = false;
  params.refineQuads = true;
  params.newQuadAlgorithm = false;

  TagFamily family("Tag36h11");
  TagDetector detector(family, params);
  TagDetectionArray detections;

  while (true) { // Constantly be reading images.
    camera >> frame;
    if (frame.empty()) {
      return 1;
    }

    cv::Point2d opticalCenter(frame.cols * .5, frame.rows * .5);
    detector.process(frame, opticalCenter, detections); // Grab detections

    // This should add the overlay from just this tile onto the frame
    processs_detections(detections, opticalCenter, &frame);

    //Finally, show the updated board image
    cv::imshow(WINDOW, frame);
    cv::waitKey(1);
  }

  return 0;
}
