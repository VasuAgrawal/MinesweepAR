header = r"""
\documentclass[]{memoir}
\usepackage{graphicx}
\usepackage[margin=0in]{geometry}
\graphicspath{{/home/hades/Downloads/tag36h11/}}

\begin{document}
"""

single_item_fmt = r"""
\begin{vplace}
	\centerline{\frame{\includegraphics[width=6in]{tag36_11_%05d}}}
\end{vplace}

\pagebreak
\begin{vplace}
	\resizebox{\linewidth}{!}{\ \ \ \ ID: %03d \ \ \ \ }
	{\huge \ } \linebreak
        {\huge \ } \linebreak
	{\huge \ } \linebreak
	\resizebox{\linewidth}{!}{\ \ \ \ \ \ \ Tag Family: 36h11\ \ \ \ \ \ \ }
\end{vplace}
\pagebreak
"""

footer = r"""
\end{document}
"""

print(header)
for i in range(586+1):
    print(single_item_fmt % (i, i))
print(footer)
