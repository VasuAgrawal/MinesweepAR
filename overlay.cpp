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
  MINE = -1,
  FLAG = -2,
};

std::map<Tile, cv::Mat> tile_images;
cv::Mat tile_mask;
cv::Mat tile_points;


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
      0.0f, tile_height,
      tile_width, tile_height,
      tile_width, 0.0f,
      0.0f, 0.0f
  );

  // All the tiles have the same mask, so it can be generated once.
  tile_mask = cv::Mat(tile_images[BLANK].size(), CV_8U, cv::Scalar(255));
}

void processs_detections(const TagDetectionArray& detections,
    const cv::Point2d opticalCenter, cv::Mat* frame) {
  static double s = .1540; // tag size in meters
  static double ss = 1.5 * s; // half tag size in meters
  static double f = 500;

  //// This can probably be 2d points?
  //static cv::Point3d corners[4] = {
      //cv::Point3d(-ss, -ss, 0),
      //cv::Point3d( ss, -ss, 0),
      //cv::Point3d( ss,  ss, 0),
      //cv::Point3d(-ss,  ss, 0),
  //};
  static cv::Mat corners = (cv::Mat_<double>(4, 3) <<
      -ss, -ss, 0,
       ss, -ss, 0,
       ss,  ss, 0,
      -ss,  ss, 0
  );

  cv::Mat K = (cv::Mat_<double>(3, 3) <<
    f, 0, opticalCenter.x,
    0, f, opticalCenter.y,
    0, 0, 1
  );

  // Shouldn't usually be static, but it's set to zeros always.
  static cv::Mat_<double> distCoeffs = cv::Mat_<double>::zeros(4,1);
  std::vector<cv::Point2d> frame_points;

  for (const auto& detection : detections) {
    if (!detection.good) continue;
    cv::Mat r, t; // Rotation and translation matrices
    CameraUtil::homographyToPoseCV(f, f, s, detection.homography, r, t);

    // From the homography that was calculated earlier, figure out the four
    // corners of the tile.
    cv::projectPoints(corners, r, t, K, distCoeffs, frame_points);

    cv::Mat H = cv::findHomography(tile_points, frame_points, 0);

    cv::Mat tile_warped;
    cv::Mat mask_warped;
    cv::warpPerspective(tile_images[Tile(detection.id % 8)], tile_warped, H,
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
