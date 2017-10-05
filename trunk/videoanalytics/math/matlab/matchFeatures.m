function [indexPairs, matchMetric] = matchFeatures(varargin)
%matchFeatures Find matching features
%   indexPairs = matchFeatures(features1, features2) returns a P-by-2
%   matrix, indexPairs, containing indices to the features most likely to
%   correspond between the two input feature matrices. The function takes
%   two inputs, features1, an M1-by-N matrix, and features2, an M2-by-N
%   matrix. features1 and features2 can also be binaryFeatures objects in 
%   the case of binary descriptors produced by the FREAK descriptor.
%
%   [indexPairs, matchMetric] = matchFeatures(features1, features2, ...)
%   also returns the metric values that correspond to the associated
%   features indexed by indexPairs in a P-by-1 matrix matchMetric.
%
%   [indexPairs, matchMetric] = matchFeatures(...,Name, Value) specifies
%   additional name-value pairs described below:
%
%   'Method'           A string used to specify how nearest neighbors
%                      between features1 and features2 are found.
%
%                      'Exhaustive': Matches features1 to the nearest
%                                    neighbors in features2 by computing
%                                    the pair-wise distance between
%                                    feature vectors in features1 and
%                                    features2.
%
%                      'Approximate': Matches features1 to the nearest
%                                     neighbors in features2 using an
%                                     efficient approximate nearest
%                                     neighbor search. Use this method for
%                                     large feature sets
% 
%                      Default: 'Exhaustive'
%
%   'MatchThreshold'   A scalar T, 0 < T <= 100, that specifies the
%                      distance threshold required for a match. A pair of
%                      features are not matched if the distance between
%                      them is more than T percent from a perfect match.
%                      Increase T to return more matches.
% 
%                      Default: 10.0 for binary feature vectors 
%                                1.0 otherwise
%
%   'MaxRatio'         A scalar R, 0 < R <= 1, specifying a ratio threshold
%                      for rejecting ambiguous matches. Increase R to
%                      return more matches.
%
%                      Default: 0.6
%
%   'Metric'           A string used to specify the distance metric. This
%                      parameter is not applicable when features1 and
%                      features2 are binaryFeatures objects.
%
%                      Possible values are:
%                        'SAD'         : Sum of absolute differences
%                        'SSD'         : Sum of squared differences 
%
%                      Default: 'SSD'
%
%                      Note: When features1 and features2 are
%                            binaryFeatures objects, Hamming distance is
%                            used to compute the similarity metric.
%
%   'Unique'           A logical scalar. Set this to true to return only
%                      unique matches between features1 and features2.
% 
%                      Default: false
%
%   Notes
%   ----- 
%   The range of values of matchMetric varies as a function of the feature
%   matching metric being used. Prior to computation of SAD and SSD
%   metrics, the feature vectors are normalized to unit vectors using the
%   L2 norm. The table below summarizes the metric ranges and perfect match
%   values:
%
%      Metric      Range                            Perfect Match Value
%      ----------  -------------------------------  ------------------- 
%      SAD         [0, 2*sqrt(size(features1, 2))]          0
%      SSD         [0, 4]                                   0
%      Hamming     [0, features1.NumBits]                   0
%   
%   Class Support
%   -------------
%   features1 and features2 can be logical, int8, uint8, int16, uint16,
%   int32, uint32, single, double, or binaryFeatures object.
%
%   The output class of indexPairs is uint32. matchMetric is double when
%   features1 and features2 are double. Otherwise, it is single.
%
%   Example 1 - Find point correspondences between two images
%   ---------------------------------------------------------
%      % Match SURF features between two images rotated and scaled with
%      % respect to each other.
%      I1 = imread('cameraman.tif');
%      I2 = imresize(imrotate(I1,-20), 1.2);
% 
%      % Detect SURF features. Other feature detectors can be used too.
%      points1 = detectSURFFeatures(I1);
%      points2 = detectSURFFeatures(I2);
% 
%      % Extract features from images
%      [f1, vpts1] = extractFeatures(I1, points1);
%      [f2, vpts2] = extractFeatures(I2, points2);
%         
%      % Match features between images 
%      indexPairs = matchFeatures(f1, f2);
%
%      matchedPoints1 = vpts1(indexPairs(:, 1));
%      matchedPoints2 = vpts2(indexPairs(:, 2));
%   
%      % Note that there are still several outliers present in the data,
%      % but otherwise you can clearly see the effects of rotation and
%      % scaling on the display of matched features.
%      figure; showMatchedFeatures(I1,I2,matchedPoints1,matchedPoints2);
%      legend('matched points 1','matched points 2');
%
%   Example 2 - Remove non-unique matches
%   -------------------------------------
%      I1 = imread('cameraman.tif');
%      I2 = imresize(imrotate(I1,-20), 1.2);
% 
%      points1 = detectSURFFeatures(I1);
%      points2 = detectSURFFeatures(I2);
% 
%      [f1, vpts1] = extractFeatures(I1, points1);
%      [f2, vpts2] = extractFeatures(I2, points2);
%         
%      % Set the 'Unique' parameter to true to remove non-unique matches
%      indexPairs = matchFeatures(f1, f2, 'Unique', true);
%
%      matchedPoints1 = vpts1(indexPairs(:, 1));
%      matchedPoints2 = vpts2(indexPairs(:, 2));
%   
%      figure; showMatchedFeatures(I1,I2,matchedPoints1,matchedPoints2);
%      legend('unique matched points 1','unique matched points 2');
%
% See also showMatchedFeatures, detectHarrisFeatures, detectFASTFeatures,
%          detectMinEigenFeatures, detectBRISKFeatures, detectSURFFeatures,
%          detectMSERFeatures, extractFeatures, estimateFundamentalMatrix,
%          estimateGeometricTransform, binaryFeatures.
 
%  Copyright 2013 The MathWorks, Inc.
%
% References
% ----------
% David Lowe, "Distinctive image features from scale-invariant keypoints",
% International Journal of Computer Vision, 60, 2 (2004)
%
% Marius Muja and David G. Lowe, "Fast Approximate Nearest Neighbors with
% Automatic Algorithm Configuration", in International Conference on
% Computer Vision Theory and Applications (VISAPP'09), 2009
%
% Marius Muja, David G. Lowe: "Fast Matching of Binary Features".
% Conference on Computer and Robot Vision (CRV) 2012.

%#codegen
%#ok<*EMCLS>
%#ok<*EMCA>

isUsingCodeGeneration = ~isempty(coder.target);

% Parse and check inputs
if isUsingCodeGeneration
    [features1, features2, metric, match_thresh, method, maxRatioThreshold, ...
        isPrenormalized, uniqueMatches, isLegacyMethod] = parseInputsCodegen(varargin{:});
else
    [features1, features2, metric, match_thresh, method, maxRatioThreshold, ...
        isPrenormalized, uniqueMatches, isLegacyMethod] = parseInputs(varargin{:});
end

checkFeatureConsistency(features1, features2);

% Match features
features1 = features1';
features2 = features2';
[index_pairs_internal, match_metric_internal] = ...
    cvalgMatchFeatures(features1, features2, ...
                       metric, match_thresh, method, maxRatioThreshold, ...
                       isPrenormalized, uniqueMatches, isLegacyMethod);
indexPairs = index_pairs_internal';
matchMetric = match_metric_internal';

%==========================================================================
% Make sure the features are of compatible classes and sizes
%==========================================================================
function checkFeatureConsistency(features1, features2)

coder.internal.errorIf(size(features1, 2) ~= size(features2, 2), ...
                       'vision:matchFeatures:featuresNotSameDimension');

%==========================================================================
% Parse and check inputs for code generation
%==========================================================================
function [features1, features2, metric, match_thresh, method, ...
    maxRatioThreshold, isPrenormalized, uniqueMatches, isLegacyMethod] = parseInputsCodegen(varargin)

eml_lib_assert(nargin == 2 || nargin > 3, ...
    'vision:matchFeatures:NotEnoughArgs', ...
    'Not enough input arguments.');

f1 = varargin{1};
f2 = varargin{2};

checkFeatures(f1);
checkFeatures(f2);

isBinaryFeature = isa(f1, 'binaryFeatures');
defaults = getDefaultParameters(isBinaryFeature);

% Set parser inputs
params = struct( ...
    'Metric',                uint32(0), ...
    'MatchThreshold',        uint32(0), ...
    'Method',                uint32(0), ...
    'MaxRatio',              uint32(0), ...
    'Prenormalized',         uint32(0), ...
    'Unique',         uint32(0));

popt = struct( ...
    'CaseSensitivity', false, ...
    'StructExpand',    true, ...
    'PartialMatching', true);

if (nargin > 3)
    % Parse parameter/value pairs
    optarg = eml_parse_parameter_inputs(params, popt, varargin{3:end});
    
    metricString = eml_get_parameter_value(optarg.Metric, ...
        defaults.Metric, varargin{3:end});    
    match_thresh  = eml_get_parameter_value(optarg.MatchThreshold, ...
        defaults.MatchThreshold, varargin{3:end});
    methodString = eml_get_parameter_value(optarg.Method, ...
        defaults.Method, varargin{3:end});
    maxRatioThreshold = eml_get_parameter_value(optarg.MaxRatio, ...
        defaults.MaxRatio, varargin{3:end});
    isPrenormalizedFromUser = eml_get_parameter_value(optarg.Prenormalized, ...
        defaults.Prenormalized, varargin{3:end});
    uniqueMatchesFromUser = eml_get_parameter_value(optarg.Unique, ...
        defaults.Unique, varargin{3:end});
    
    % Check parameters
    
    metric = checkMetric(metricString);
    checkMatchThreshold(match_thresh);
    method = checkMatchMethod(methodString);
    checkMaxRatioThreshold(maxRatioThreshold);   
    checkPrenormalized(isPrenormalizedFromUser);
    checkUniqueMatches(uniqueMatchesFromUser);    
    isPrenormalized = logical(isPrenormalizedFromUser);
    uniqueMatches   = logical(uniqueMatchesFromUser);
    
    isMethodSetByUser        = logical(optarg.Method);
    isPrenormalizedSetByUser = logical(optarg.Prenormalized);

else
    metric = defaults.Metric;
    match_thresh = defaults.MatchThreshold;
    method = defaults.Method;
    maxRatioThreshold = defaults.MaxRatio;
    isPrenormalized = logical(defaults.Prenormalized);
    uniqueMatches = logical(defaults.Unique);
    
    isMethodSetByUser        = false;
    isPrenormalizedSetByUser = false;

end

crossCheckMetricAndMethod(isMethodSetByUser, metric, method);

crossCheckPrenormalizedAndMethod(isPrenormalizedSetByUser, ...
    isMethodSetByUser, method);

isLegacyMethod = isLegacy(method);

[features1, features2, metric] = assignFeaturesAndMetric(f1, f2, metric);

%==========================================================================
% Parse and check inputs
%==========================================================================
function [features1, features2, metric, match_thresh, method,...
    maxRatioThreshold, isPrenormalized, uniqueMatches, isLegacyMethod] ...
    = parseInputs(varargin)

if nargin >= 1
    isBinaryFeature = isa(varargin{1}, 'binaryFeatures');
else
    isBinaryFeature = false;
end

defaults = getDefaultParameters(isBinaryFeature);

% Setup parser
parser = inputParser;
parser.addRequired('features1', @checkFeatures);
parser.addRequired('features2', @checkFeatures);
parser.addParameter('MatchThreshold', defaults.MatchThreshold, ...
    @checkMatchThreshold);
parser.addParameter('Method', defaults.Method);
parser.addParameter('MaxRatio', defaults.MaxRatio, ...
    @checkMaxRatioThreshold);
parser.addParameter('Metric', defaults.Metric);
parser.addParameter('Prenormalized', defaults.Prenormalized, ...
    @checkPrenormalized);
parser.addParameter('Unique', defaults.Unique, ...
    @checkUniqueMatches);

% Parse input
parser.parse(varargin{:});

method = checkMatchMethod(parser.Results.Method);
metric = checkMetric(parser.Results.Metric);

match_thresh = parser.Results.MatchThreshold;
maxRatioThreshold = parser.Results.MaxRatio;
isPrenormalized = logical(parser.Results.Prenormalized);
uniqueMatches = logical(parser.Results.Unique);

isLegacyMethod = isLegacy(method);

% Issue warnings or errors if using new parameters with legacy method
% values.
if isLegacyMethod
        
    if strcmpi(method, 'nearestneighbor_old')
        % issue deprecation warning
        warning(message('vision:matchFeatures:deprecatedNNOld'));
                        
    end    
    
    % Unique cannot be used with old Method values
    if isParameterSetByCaller('Unique', parser)
        error(message('vision:matchFeatures:invalidMethodForUnique'));        
    end
                 
    %'MaxRatio' only has meaning if the 'Method' is 'NearestNeighborRatio'
    %(for backward compatibility), Exhaustive, or Approximate.
    if ~strcmpi(method, 'nearestneighborratio') ...
            && isParameterSetByCaller('MaxRatio', parser)
        warning(message('vision:matchFeatures:maxRatioUnused'));        
    end
                       
end

isMethodSetByUser        = isParameterSetByCaller('Method', parser);
isPrenormalizedSetByUser = isParameterSetByCaller('Prenormalized', parser);

crossCheckMetricAndMethod(isMethodSetByUser, metric, method);

crossCheckPrenormalizedAndMethod(isPrenormalizedSetByUser, ...
    isMethodSetByUser, method);

if isBinaryFeature
    if isPrenormalizedSetByUser
        warning(message('vision:matchFeatures:binParamUnused', 'Prenormalized'));
    end
    
    if isParameterSetByCaller('Metric', parser)
        warning(message('vision:matchFeatures:binParamUnused', 'Metric'));
    end
end

f1 = parser.Results.features1;
f2 = parser.Results.features2;

[features1, features2, metric] = assignFeaturesAndMetric(f1, f2, metric);

%==========================================================================
function [features1, features2, metric] = assignFeaturesAndMetric(f1, f2,...
    temp_metric)

% Handle the case when features are of class binaryFeatures

coder.internal.errorIf(~isequal(class(f1), class(f2)),...
     'vision:matchFeatures:featuresNotSameClass');

% Assign outputs
if isa(f1, 'binaryFeatures')
    features1 = f1.Features;
    features2 = f2.Features;
    metric = 'hamming'; % assume default metric for the binary features
else
    features1 = f1;
    features2 = f2;
    metric = lower(temp_metric);
end

%==========================================================================
function tf = isParameterSetByCaller(param, parser)
tf = strcmp(param, parser.UsingDefaults);
tf = ~any(tf);

%==========================================================================
function checkFeatures(features)
validateattributes(features, {'logical', 'int8', 'uint8', 'int16', ...
    'uint16', 'int32', 'uint32', 'single', 'double', 'binaryFeatures'}, ...
    {'2d', 'nonsparse', 'real'}, 'matchFeatures', 'FEATURES');

%========================================================================== 
function matchedValue = checkMetric(value)
list = {'ssd', 'normxcorr', 'sad'};
validateattributes(value, {'char'}, {'nonempty'}, 'matchFeatures', ...
    'Metric');

matchedValue = validatestring(value, list, 'matchFeatures', 'Metric');   

%========================================================================== 
function matchedValue = checkMatchMethod(value)
list = {'nearestneighborratio', 'threshold', 'nearestneighborsymmetric',...
    'nearestneighbor_old','approximate','exhaustive'};
validateattributes(value, {'char'}, {'nonempty'}, 'matchFeatures', ...
    'Method');
matchedValue = validatestring(value, list, 'matchFeatures', 'Method');

%==========================================================================
function checkMatchThreshold(threshold)
validateattributes(threshold, {'numeric'}, {'nonempty', 'nonnan', ...
    'finite', 'nonsparse', 'real', 'positive', 'scalar', '<=', 100}, ...
    'matchFeatures', 'MatchThreshold');

%==========================================================================
function checkMaxRatioThreshold(threshold)
validateattributes(threshold, {'numeric'}, {'nonempty', 'nonnan', ...
    'finite', 'nonsparse', 'real', 'positive', 'scalar', '<=', 1.0}, ...
    'matchFeatures', 'MaxRatioThreshold');

%==========================================================================
function checkPrenormalized(isPrenormalized)
validateattributes(isPrenormalized, {'logical','numeric'}, ...
    {'nonempty', 'scalar', 'real', 'nonnan', 'nonsparse'}, ...
    'matchFeatures', 'Prenormalized');

%==========================================================================
function checkUniqueMatches(uniqueMatches)
validateattributes(uniqueMatches, {'logical','numeric'}, ...
    {'nonempty', 'scalar', 'real', 'nonnan', 'nonsparse'}, ...
    'matchFeatures', 'Unique');

%==========================================================================
function crossCheckMetricAndMethod(isMethodSetByUser, metric, method)

if isMethodSetByUser
    % only check if Method is user specified. Otherwise an error can break
    % backward compatibility.
    invalidUseOfNormxcorr = ...
        (strcmpi(method, 'approximate') || strcmpi(method, 'exhaustive')) ...
        && strcmpi(metric, 'normxcorr');
    
    coder.internal.errorIf(invalidUseOfNormxcorr, ...
        'vision:matchFeatures:invalidMethodForNormxcorr');
end

%==========================================================================
function crossCheckPrenormalizedAndMethod(userDefinedPrenormalized, ...
    userDefinedMethod, method)

% only check if the user specifies the Method. This preserves backward
% compatibility when the user only specified the Prenormalized parameter.
if userDefinedMethod 
    invalidMethodForPrenormalized = userDefinedPrenormalized ...
        && (strcmpi(method, 'approximate') || strcmpi(method, 'exhaustive'));
    
    coder.internal.errorIf(invalidMethodForPrenormalized, ...
        'vision:matchFeatures:invalidMethodForPrenormalized');
end

%==========================================================================
function defaults = getDefaultParameters(isBinaryFeature)

if isBinaryFeature
    thresh = 10.0;
else
    thresh = 1.0;
end

defaults = struct(...
    'Metric', 'ssd', ...
    'MatchThreshold',  thresh, ...
    'Method', 'exhaustive',...
    'MaxRatio', 0.6,...
    'Prenormalized', false,...
    'Unique', false);

%==========================================================================
% Return true if using one of the legacy Method values
%==========================================================================
function tf = isLegacy(method)

tf = strcmpi(method,'threshold') ...
    || strcmpi(method,'nearestneighborratio')...
    || strcmpi(method,'nearestneighborsymmetric')...
    || strcmpi(method,'nearestneighbor_old');