%SURFPoints Object for storing SURF interest points
%
%   SURFPoints object describes SURF interest points.
%
%   POINTS = SURFPoints(LOCATION, PARAM1, VAL1, PARAM2, VAL2, ...)
%   constructs a SURFPoints object from an M-by-2 array of [x y] 
%   coordinates, LOCATION, and optional input parameters listed below.
%   Note that each additional parameter can be specified as a scalar or 
%   a vector whose length matches the number of coordinates in LOCATION. 
%   The available parameters are:
%
%   'Scale'       Value greater than or equal to 1.6. Specifies scale at 
%                 which the interest points were detected.
%
%                 Default: 1.6
%
%   'Metric'      Value describing strength of detected feature. SURF 
%                 algorithm uses a determinant of approximated Hessian.
%
%                 Default: 0.0
%
%   'SignOfLaplacian' -1, 0, or 1 integer indicating sign of the Laplacian
%                 determined during the detection process.  This value
%                 can be used to accelerate the feature matching process.
%
%                 Default: 0
%
%   'Orientation' Value, in radians, describing orientation of the detected
%                 feature. It is typically set during the descriptor 
%                 extraction process. extractFeatures function modifies
%                 the default value of 0. See extractFeatures for further 
%                 details.
%
%                 Default: 0.0
%
%   Notes:
%   ======
%   - The main purpose of this class is to pass the data between
%     detectSURFFeatures and extractFeatures functions. It can also be used
%     to manipulate and plot the data returned by these functions.
%     Using the class to fill the points interactively is considered an
%     advanced maneuver. It is useful in situations where you might want to
%     mix a non-SURF interest point detector with a SURF descriptor.
%   - 'Orientation' is specified as an angle, in radians, as measured from
%     the X-axis with the origin at 'Location'.  'Orientation' should not 
%     be set manually.  You should rely on the call to extractFeatures for 
%     filling this value. 'Orientation' is mainly useful for visualization
%     purposes.
%   - 'SignOfLaplacian' is a property unique to SURF detector. Blobs with 
%     identical metric values but different signs of Laplacian will differ 
%     by their intensity values: white blob on black background vs. black
%     blob on white background. This value can be used to quickly eliminate
%     blobs that don't match in this sense. For non-SURF detectors, this 
%     value is not relevant although it should be set consistently as to
%     not affect the matching process.  For example, for corner features, 
%     you can simply use the default value of 0.
%   - Note that SURFPoints is always a scalar object which may hold many
%     points. Therefore, NUMEL(surfPoints) always returns 1. This may be 
%     different from LENGTH(surfPoints), which returns the true number
%     of points held by the object.
%
%   SURFPoints methods:
%      selectStrongest  - Select N interest points with strongest metrics
%      selectUniform    - Select N uniformly spaced interest points
%      plot             - Plot SURF points
%      length           - Return number of stored points
%      isempty          - Return true for empty SURFPoints object
%      size             - Return size of the SURFPoints object
%
%   SURFPoints properties:
%      Count            - Number of points held by the object
%      Location         - Matrix of [X,Y] point coordinates
%      Scale            - Scale at which the feature was detected
%      Metric           - Strength of each feature
%      SignOfLaplacian  - Sign of the Laplacian
%      Orientation      - Orientation assigned to the feature during 
%                         the descriptor extraction process
%
%   Example 1
%   ---------
%   % Detect SURF features
%   I = imread('cameraman.tif');
%   points = detectSURFFeatures(I);
%   % Display 10 strongest points in an image and on command line
%   strongest = points.selectStrongest(10);
%   imshow(I); hold on;
%   plot(strongest);   % show location and scale
%   strongest.Location % display [x y] coordinates
%
%   Example 2
%   ---------
%   % Detect SURF features
%   I = imread('cameraman.tif');
%   points = detectSURFFeatures(I);
%   % Display the last 5 points
%   imshow(I); hold on;
%   plot(points(end-4:end));
%
% See also detectSURFFeatures, extractFeatures, matchFeatures,
%          detectSURFFeatures, detectHarrisFeatures,
%          detectMinEigenFeatures, detectFASTFeatures, MSERRegions, 
%          cornerPoints

% Copyright 2010-2011 The MathWorks, Inc.

classdef SURFPoints < vision.internal.SURFPointsImpl & vision.internal.FeaturePoints                 
   
   methods(Access=private, Static)
       function name = matlabCodegenRedirect(~)
         name = 'vision.internal.SURFPoints_cg';
       end
   end
   
   %-----------------------------------------------------------------------
   methods (Access='public')
       
       function this = SURFPoints(varargin)                      
           this = this@vision.internal.SURFPointsImpl(varargin{:});                             
       end  
              
       %-------------------------------------------------------------------
       function varargout = plot(this, varargin)
           %plot Plot SURF points
           %
           %   surfPoints.plot plots SURF points in the current axis.
           %
           %   surfPoints.plot(AXES_HANDLE,...) plots using axes with
           %   the handle AXES_HANDLE instead of the current axes (gca).
           %
           %   surfPoints.plot(AXES_HANDLE, PARAM1, VAL1, PARAM2,
           %   VAL2, ...) controls additional plot parameters:
           %
           %      'showScale'   true or false.  When true, a circle 
           %                    proportional to the scale of the detected
           %                    feature is drawn around the point's
           %                    location
           %
           %                    Default: true
           %
           %      'showOrientation' true or false. When true, a line
           %                    corresponding to the point's orientation 
           %                    is drawn from the point's location to the
           %                    edge of the circle indicating the scale
           %
           %                    Default: false
           %
           %   Notes
           %   -----
           %   - Scale of the feature is represented by a circle of
           %     6*Scale radius, which is equivalent to the size of
           %     circular area used by the SURF algorithm to compute 
           %     orientation of the feature
           %
           %   Example
           %   -------
           %   % Extract SURF features
           %   I = imread('cameraman.tif');
           %   points = detectSURFFeatures(I);
           %   [features, valid_points] = extractFeatures(I, points);
           %   % Visualize 10 strongest SURF features, including their 
           %   % scales and orientation which were determined during the 
           %   % descriptor extraction process.
           %   imshow(I); hold on;
           %   strongestPoints = valid_points.selectStrongest(10);
           %   strongestPoints.plot('showOrientation',true);
              
           nargoutchk(0,1);           
           
           supportsScaleAndOrientation = true;
           
           this.PlotScaleFactor = 6;
           
           h = plot@vision.internal.FeaturePoints(this, ...
               supportsScaleAndOrientation, varargin{:});
           
           if nargout == 1
               varargout{1} = h;
           end
           
       end             
   end
              
   methods (Access='public', Hidden=true)
       %-------------------------------------------------------------------
       function this = append(this,varargin)
           %append Appends additional SURF points
           
           indexS = this.Count + 1;
           inputs = parseInputs(this, varargin{:});
           indexE = indexS + size(inputs.Location,1) - 1;
           
           this.pLocation(indexS:indexE, 1:2)     = inputs.Location;
           this.pScale(indexS:indexE, 1)          = inputs.Scale;
           this.pMetric(indexS:indexE, 1)         = inputs.Metric;
           this.pSignOfLaplacian(indexS:indexE,1) = inputs.SignOfLaplacian;
           this.pOrientation(indexS:indexE, 1)    = inputs.Orientation;
       end
   end
   
   methods (Access='protected')
       %-------------------------------------------------------------------
       % Copy data for subsref. This method is used in subsref
       function this = subsref_data(this, option)
           this = subsref_data@vision.internal.FeaturePoints(this, option);
           
           % Scale, SignOfLaplacian, and Orientation are Mx1 matrices. When
           % the indices for sub-referencing is a 1-D array, we explicitly
           % specify the size for the second dimension.
           if length(option.subs) == 1
               option.subs{2} = 1;
           end
           
           this.pScale           = subsref(this.pScale,option);
           this.pSignOfLaplacian = subsref(this.pSignOfLaplacian,option);
           this.pOrientation     = subsref(this.pOrientation,option);
       end       
       
       %-------------------------------------------------------------------
       % Copy data for subsasgn. This method is used in subsasgn
       function this = subsasgn_data(this, option, in)
           this = subsasgn_data@vision.internal.FeaturePoints(this, option, in);
           
           if isempty(in)
               this.pScale = ...
                   subsasgn(this.pScale, option, in);
               this.pSignOfLaplacian = ...
                   subsasgn(this.pSignOfLaplacian, option, in);
               this.pOrientation = ...
                   subsasgn(this.pOrientation, option, in);
           else
               this.pScale = ...
                   subsasgn(this.pScale, option, in.pScale);
               this.pSignOfLaplacian = ...
                   subsasgn(this.pSignOfLaplacian, option, in.pSignOfLaplacian);
               this.pOrientation = ...
                   subsasgn(this.pOrientation, option, in.pOrientation);
           end
       end
       %------------------------------------------------------------------
       % Concatenate data for vertcat. This method is used in vertcat.
       %------------------------------------------------------------------
       function obj = vertcatObj(varargin)
           obj = varargin{1};
           
           for i=2:nargin
               obj.pLocation    = [obj.pLocation; varargin{i}.pLocation];
               obj.pMetric      = [obj.pMetric  ; varargin{i}.pMetric];
               obj.pScale       = [obj.pScale   ; varargin{i}.pScale];
               obj.pOrientation = [obj.pOrientation ; varargin{i}.pOrientation];
               obj.pSignOfLaplacian = [obj.pSignOfLaplacian ; varargin{i}.pSignOfLaplacian];
           end
       end
   end
   
end

% LocalWords:  Laplacian
% LocalWords:  OpenCV
