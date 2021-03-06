# Python 2/3 compatability
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
import sys

import argparse
import numpy as np
import cv2
import math

FLANN_INDEX_KDTREE = 1  # bug: flann enums are missing
FLANN_INDEX_LSH    = 6

def anorm2(a):
    return (a*a).sum(-1)

def anorm(a):
    return np.sqrt(anorm2(a))

def filter_matches(kp1, kp2, matches, ratio = 0.75):
    mkp1, mkp2 = [], []
    for m in matches:
        if len(m) == 2 and m[0].distance < m[1].distance * ratio:
            m = m[0]
            mkp1.append( kp1[m.queryIdx] )
            mkp2.append( kp2[m.trainIdx] )
    p1 = np.float32([kp.pt for kp in mkp1])
    p2 = np.float32([kp.pt for kp in mkp2])
    kp_pairs = zip(mkp1, mkp2)
    return p1, p2, list(kp_pairs)

def init_feature(name):
    chunks = name.split('-')
    if chunks[0] == 'sift':
        detector = cv2.xfeatures2d.SIFT_create()
        norm = cv2.NORM_L2
    elif chunks[0] == 'surf':
        detector = cv2.xfeatures2d.SURF_create(800)
        norm = cv2.NORM_L2
    elif chunks[0] == 'orb':
        detector = cv2.ORB_create(400)
        norm = cv2.NORM_HAMMING
    elif chunks[0] == 'akaze':
        detector = cv2.AKAZE_create()
        norm = cv2.NORM_HAMMING
    elif chunks[0] == 'brisk':
        detector = cv2.BRISK_create()
        norm = cv2.NORM_HAMMING
    else:
        return None, None
    if 'flann' in chunks:
        if norm == cv2.NORM_L2:
            flann_params = dict(algorithm = FLANN_INDEX_KDTREE, trees = 5)
        else:
            flann_params= dict(algorithm = FLANN_INDEX_LSH,
                               table_number = 6, # 12
                               key_size = 12,     # 20
                               multi_probe_level = 1) #2
        matcher = cv2.FlannBasedMatcher(flann_params, {})  # bug : need to pass empty dict (#1329)
    else:
        matcher = cv2.BFMatcher(norm)
    return detector, matcher

def explore_match(win, img1, img2, kp_pairs, direction=0, status = None, H = None):
    h1, w1 = img1.shape[:2]
    h2, w2 = img2.shape[:2]
    vis = np.zeros((max(h1, h2), w1+w2), np.uint8)
    vis[:h1, :w1] = img1
    vis[:h2, w1:w1+w2] = img2
    vis = cv2.cvtColor(vis, cv2.COLOR_GRAY2BGR)

    if H is not None:
        corners = np.float32([[0, 0], [w1, 0], [w1, h1], [0, h1]])
        corners = np.int32( cv2.perspectiveTransform(corners.reshape(1, -1, 2), H).reshape(-1, 2) + (w1, 0) )
        cv2.polylines(vis, [corners], True, (255, 255, 255))

    if status is None:
        status = np.ones(len(kp_pairs), np.bool_)
    p1, p2 = [], []  # python 2 / python 3 change of zip unpacking
    for kpp in kp_pairs:
        p1.append(np.int32(kpp[0].pt))
        p2.append(np.int32(np.array(kpp[1].pt) + [w1, 0]))

    green = (0, 255, 0)
    red = (0, 0, 255)
    kp_color = (51, 103, 236)
    for (x1, y1), (x2, y2), inlier in zip(p1, p2, status):
        if inlier:
            col = green
            cv2.circle(vis, (x1, y1), 2, col, -1)
            cv2.circle(vis, (x2, y2), 2, col, -1)
        else:
            col = red
            r = 2
            thickness = 3
            cv2.line(vis, (x1-r, y1-r), (x1+r, y1+r), col, thickness)
            cv2.line(vis, (x1-r, y1+r), (x1+r, y1-r), col, thickness)
            cv2.line(vis, (x2-r, y2-r), (x2+r, y2+r), col, thickness)
            cv2.line(vis, (x2-r, y2+r), (x2+r, y2-r), col, thickness)
    for (x1, y1), (x2, y2), inlier in zip(p1, p2, status):
        if inlier:
            cv2.line(vis, (x1, y1), (x2, y2), green)
    font = cv2.FONT_HERSHEY_SIMPLEX
    cv2.putText(vis, str(direction), (20, 100), font, 4, (255, 0, 255), 2, cv2.LINE_AA)
    cv2.imshow(win, vis)
    return vis

if __name__=='__main__':
    # Handle CLI params
    parser = argparse.ArgumentParser()
    parser.add_argument('--input', type=str, default='test.avi')
    parser.add_argument('--step_frame', type=int, default=5)
    parser.add_argument('--plot_angle', type=float, default=0.0)
    FLAGS = parser.parse_args()

    # Open capture device. Use 0,1,2...etc for webcams
    cap = cv2.VideoCapture(FLAGS.input)
    if not cap.isOpened():
        print ('Unable open input device')
        sys.exit(-1)

    # Determine sequence params
    vidHeight = cap.get(cv2.CAP_PROP_FRAME_WIDTH)
    vidWidth = cap.get(cv2.CAP_PROP_FRAME_HEIGHT)
    frameRate = cap.get(cv2.CAP_PROP_FPS)
    nframes = cap.get(cv2.CAP_PROP_FRAME_COUNT)

    print(vidHeight, vidWidth, frameRate)

    step_frame = FLAGS.step_frame
    plot_angle = FLAGS.plot_angle

    FrameRez = []
    DirectionMov = []

    cur_frame = None
    prev_frame = None

    detector, matcher = init_feature('akaze')
    # detector, matcher = init_feature('brisk-flann')
    
    fourcc = cv2.VideoWriter_fourcc(*'XVID')
    out = cv2.VideoWriter('output.avi', fourcc, 6.0, (2*640, 480))
    cnt = 0
    while True:
        ret, frame = cap.read() # read frame
        if not ret:
            print ('End of the sequence')
            break

        if cnt != step_frame: # skip step frames
            cnt += 1
            continue
        cnt = 0

        cur_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        if type(prev_frame) is np.ndarray: # if it is not the first frame
            # Feature detection on the frame ¹1
            prev_frame_kp, prev_frame_desc = detector.detectAndCompute(prev_frame, None)
            # Feature detection on the frame ¹2
            cur_frame_kp, cur_frame_desc = detector.detectAndCompute(cur_frame, None)
            # Feature matching
            raw_matches = matcher.knnMatch(prev_frame_desc, cur_frame_desc, k=2)
            # Feature matching filtering
            original_points, distorted_points, kp_pairs = filter_matches(prev_frame_kp, cur_frame_kp, raw_matches)

            if len(original_points) >= 4:
                #
                H, status = cv2.findHomography(original_points, distorted_points, cv2.RANSAC, 5.0)
                #print('%d / %d  inliers/matched' % (np.sum(status), len(status)))
                ### Direction calculation
                rez_vec_1_x = []
                rez_vec_1_y = []
                rez_vec_2_x = []
                rez_vec_2_y = []
                for idx in range(len(status)):
                    if status[idx] == 1:
                        rez_vec_1_x.append(original_points[idx, 1])
                        rez_vec_1_y.append(original_points[idx, 0])
                        rez_vec_2_x.append(distorted_points[idx, 1])
                        rez_vec_2_y.append(distorted_points[idx, 0])
                rez_vec_1_x = np.array(rez_vec_1_x)
                rez_vec_1_y = np.array(rez_vec_1_y)
                rez_vec_2_x = np.array(rez_vec_2_x)
                rez_vec_2_y = np.array(rez_vec_2_y)


                K = (rez_vec_2_y-rez_vec_1_y)/((rez_vec_2_x-rez_vec_1_x)+0.000001)
                colich = len(rez_vec_1_x)
                Alfa = np.zeros(colich, dtype=np.float32)

                for tt in range(colich):
                    # 1
                    if (rez_vec_2_x[tt] >= rez_vec_1_x[tt]) and (rez_vec_2_y[tt] >= rez_vec_1_y[tt]):
                        Alfa[tt] = np.floor((np.arctan((K[tt]))* 180. / math.pi))
                        # 2
                    elif (rez_vec_1_x[tt] > rez_vec_2_x[tt]) and (rez_vec_2_y[tt] >= rez_vec_1_y[tt]):
                        Alfa[tt] = 180 - np.abs(np.floor((np.arctan((K[tt])) * 180. / math.pi)))
                        # 3
                    elif (rez_vec_1_x[tt] >= rez_vec_2_x[tt]) and (rez_vec_1_y[tt] > rez_vec_2_y[tt]):
                        Alfa[tt] = 180 + np.abs(np.floor((np.arctan((K[tt])) * 180. / math.pi)))
                        # 4
                    elif (rez_vec_2_x[tt] > rez_vec_1_x[tt]) and (rez_vec_1_y[tt] > rez_vec_2_y[tt]):
                        Alfa[tt] = 360 - np.abs(np.floor((np.arctan((K[tt])) * 180. / math.pi)))

                alfa = np.median(Alfa)
                beta = 0

                if ((alfa >= 337) and (alfa <= 360)) or ((alfa >= 0) and (alfa < 22.5)):
                    beta = 5

                if (alfa >= 22.5) and (alfa < 67.5):
                    beta = 6

                if (alfa >= 67.5) and (alfa < 112.5):
                    beta = 7

                if (alfa >= 112.5) and (alfa < 157.5):
                    beta = 8

                if (alfa >= 157.5) and (alfa < 202.5):
                    beta = 1

                if (alfa >= 202.5) and (alfa < 247.5):
                    beta = 2

                if (alfa >= 247.5) and (alfa < 292.5):
                    beta = 3

                if (alfa >= 292.5) and (alfa < 337.5):
                    beta = 4

                vis = explore_match('out', prev_frame, cur_frame, kp_pairs, beta, status, H)
                out.write(vis)
            else:
                H, status = None, None
                print('%d matches found, not enough for homography estimation' % len(original_points))

        # Update previous frame
        ch = cv2.waitKey(1)
        if ch == 27:
            break
        prev_frame = cur_frame.copy()

    cap.release()
    out.release()
    cv2.destroyAllWindows()
