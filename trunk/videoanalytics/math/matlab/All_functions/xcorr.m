function [c,lags] = xcorr(x,varargin)
%XCORR Cross-correlation function estimates.
%   C = XCORR(A,B), where A and B are length M vectors (M>1), returns
%   the length 2*M-1 cross-correlation sequence C. If A and B are of
%   different length, the shortest one is zero-padded. C will be a
%   row vector if A is a row vector, and a column vector if A is a
%   column vector.
%
%   XCORR produces an estimate of the correlation between two random
%   (jointly stationary) sequences:
%          C(m) = E[A(n+m)*conj(B(n))] = E[A(n)*conj(B(n-m))]
%   It is also the deterministic correlation between two deterministic
%   signals.
%
%   C = XCORR(A), where A is a length M vector, returns the length 2*M-1
%   auto-correlation sequence C. The zeroth lag of the output correlation
%   is in the middle of the sequence, at element M.
%
%   C = XCORR(A), where A is an M-by-N matrix (M>1), returns a large matrix
%   with 2*M-1 rows and N^2 columns containing the cross-correlation
%   sequences for all combinations of the columns of A; the first N columns
%   of C contain the delays and cross correlations using the first column
%   of A as the reference, the next N columns of C contain the delays and
%   cross correlations using the second column of A as the reference, and
%   so on.
%
%   C = XCORR(...,MAXLAG) computes the (auto/cross) correlation over the
%   range of lags:  -MAXLAG to MAXLAG, i.e., 2*MAXLAG+1 lags.
%   If missing, default is MAXLAG = M-1.
%
%   [C,LAGS] = XCORR(...)  returns a vector of lag indices (LAGS).
%
%   XCORR(...,SCALEOPT), normalizes the correlation according to SCALEOPT:
%     'biased'   - scales the raw cross-correlation by 1/M.
%     'unbiased' - scales the raw correlation by 1/(M-abs(lags)).
%     'coeff'    - normalizes the sequence so that the auto-correlations
%                  at zero lag are identically 1.0.
%     'none'     - no scaling (this is the default).
%
%   % Example:
%   %   Measure the delay between two correlated signals.
%
%   load noisysignals s1 s2;  % load sensor signals
%   [acor,lag] = xcorr(s2,s1);
%   [~,I] = max(abs(acor));
%   timeDiff = lag(I)         % sensor 2 leads sensor 1 by 350 samples
%   subplot(311); plot(s1); title('s1');
%   subplot(312); plot(s2); title('s2');
%   subplot(313); plot(lag,acor);
%   title('Cross-correlation between s1 and s2')
%
%   See also FINDDELAY, XCORR2, XCOV, CORRCOEF, CONV, CCONV, COV, DTW.

%   Copyright 1988-2015 The MathWorks, Inc.

%   References:
%     S.J. Orfanidis, "Optimum Signal Processing. An Introduction"
%     2nd Ed. Macmillan, 1988.

%#codegen
% Limitations for code generation:
% * All leading dimensions of length 1 for X inputs must be constant-length
%   dimensions. If X is known to be a row-vector but size(X,1) is not
%   a constant 1, use XCORR(X(:).',...) rather than XCORR(X,...).

narginchk(1,4);

if ~isempty(coder.target)
    % In code generation this encourages the compiler to produce
    % specialized code with constant inputs.
    coder.internal.prefer_const(varargin);
    if coder.internal.isAmbiguousTypes
        % This is only executed during Simulink's size propagation pass
        % when XCORR is used in a MATLAB Function Block. Compute zero
        % outputs of the correct sizes and return.
        [c,lags] = zeroOutputs(x,varargin{:});
        return
    end
end

% Transform the input, if necessary, so that computations can be performed
% on columns.
if needsShifting(x)
    % Make a recursive call. Row vector input becomes a column vector, N-D
    % inputs have leading ones (the constant leading ones) shifted out.
    % Note: With code generation SHIFTDIM will issue a run-time error if
    % the first dimension without a constant length of 1 has a length of 1
    % at run-time.
    [x1,nshift] = shiftdim(x);
    if nargout == 2
        [c1,lags] = xcorr(x1,varargin{:});
    else
        c1 = xcorr(x1,varargin{:});
    end
    c = shiftdim(c1,-nshift);
    return
end

% Determine which options have been supplied and, if so, where they have
% been supplied in the varargin inputs.
[ySupplied,maxlagIdx,scaleIdx] = sortInputs(varargin{:});

% Calculate the cross-correlation.
if ySupplied
    % Cross-correlation of two column vectors.
    coder.internal.assert(isvector(varargin{1}), ...
        'signal:xcorr:BMustBeVector','B');
    coder.internal.assert(iscolumn(x), ...
        'signal:xcorr:MismatchedAB','B','A');
    y = varargin{1}(:); % Make y a column vector.
    defaultMaxlag = max(size(x,1),size(y,1)) - 1;
    % If it has been supplied, fetch maxlag from varargin and validate it.
    maxlag = fetchMaxlag(maxlagIdx,defaultMaxlag,varargin{:});
    % Perform the cross-correlation.
    c1 = crosscorr(x,y,maxlag);
    % Scale the output, if requested.
    if scaleIdx > 0
        c1 = scaleXcorr(varargin{scaleIdx},c1,x,y);
    end
else
    % Perform all auto- and cross-correlations of the columns of x.
    defaultMaxlag = size(x,1) - 1;
    % If it has been supplied, fetch maxlag from varargin and validate it.
    maxlag = fetchMaxlag(maxlagIdx,defaultMaxlag,varargin{:});
    % Peform the auto- and cross-correlations.
    c1 = autocorr(x,maxlag);
    % Scale the output, if requested.
    if scaleIdx > 0
        c1 = scaleXcorr(varargin{scaleIdx},c1,x);
    end
end

% Pad the output with zeros if maxlag > defaultMaxlag.
c = padOutput(c1,maxlag,defaultMaxlag);

if nargout == 2
    lags = -maxlag:maxlag;
end

%--------------------------------------------------------------------------

function c = autocorr(x,maxlag)
% Compute all possible auto- and cross-correlations of the columns of a
% matrix input x. Output is clipped based on maxlag but not padded when
% maxlag >= size(x,1).
coder.internal.prefer_const(maxlag);
[m,n] = size(x);
mxl = min(maxlag,m - 1);
ceilLog2 = nextpow2(2*m - 1);
m2 = 2^ceilLog2;

if ~isempty(coder.target)
    % Perform some tasks specific to code generation.
    % Size inference may need some help to bound m2. The following
    % assertion can never fail, since 2^nextpow2(k) <= 2*k - 1 when k is an
    % integer. Here k = 2*m - 1.
    assert(m2 <= 4*m - 3);
    if n == 1
        % Compare estimates of the cost of time domain and frequency domain
        % calculations and choose the lower of the two.
        % Estimate the frequency domain calculation cost.
        fdops = m2*(15*ceilLog2 + 6);
        % Estimate time domain calculation cost.
        tdops = OpCountForAutocorrTD(mxl,isreal(x),m);
        if tdops < fdops
            % Do the computation in the time domain and return.
            c = autocorrTD(x,mxl);
            return
        end
    end
end

if n == 1
    % Autocorrelation of a column vector.
    X = fft(x,m2,1);
    Cr = abs(X).^2;
    if isreal(x)
        c1 = real(ifft(Cr,[],1));
    else
        c1 = ifft(Cr,[],1);
    end
    % Keep only the lags we want and move negative lags before positive
    % lags.
    c = [c1(m2 - mxl + (1:mxl)); c1(1:mxl+1)];
else
    % Auto- and cross-correlation of the columns of a matrix.
    X = fft(x,m2,1);
    % The number of rows of X is M2 (a power of 2), and X has N columns. We
    % want to perform all possible element-wise multiplications of the
    % columns of X by the columns of conj(X). To do this efficiently, we
    % use BSXFUN, asking it to expand one operand in the second dimension
    % and the other in the third. Note that X should already be m2-by-n,
    % but if we reshape anyway via X(:,:), the code will be robust when
    % operating on N-D inputs.
    C = bsxfun(@times,reshape(X,m2,1,n),conj(X(:,:)));
    % Call IFFT and force real output if x is real.
    if isreal(x)
        c1 = real(ifft(C,[],1));
    else
        c1 = ifft(C,[],1);
    end
    % c1 is M2-by-N-by-N.
    % Keep only the lags we want, and move the negative lags before the
    % positive lags. Also flatten the result to 2-D.
    c = [c1(m2 - mxl + (1:mxl),:); c1(1:mxl+1,:)];
end

%--------------------------------------------------------------------------

function c = crosscorr(x,y,maxlag)
% Compute cross-correlation for vector inputs. Output is clipped based on
% maxlag but not padded if maxlag >= max(size(x,1),size(y,1)).
coder.internal.prefer_const(maxlag);
m = max(size(x,1),size(y,1));
mxl = min(maxlag,m - 1);
ceilLog2 = nextpow2(2*m - 1);
m2 = 2^ceilLog2;

if ~isempty(coder.target)
    % Perform some tasks specific to code generation.
    % Size inference may need some help to bound m2. The following
    % assertion never fires, since 2^nextpow2(k) <= 2*k - 1 when k is an
    % integer. Here k = 2*m - 1.
    assert(m2 <= 4*m - 3);
    % Compare estimates of the cost of time domain and frequency domain
    % calculations and choose the lower of the two.
    % Estimate the frequency domain calculation cost.
    fdops = m2*(15*ceilLog2 + 6);
    % Estimate time domain calculation cost.
    tdops = OpCountForXcorrTD(mxl,isreal(x),size(x,1),size(y,1));
    if tdops < fdops
        % Do the computation in the time domain.
        c = crosscorrTD(x,y,mxl);
        return
    end
end

X = fft(x,m2,1);
Y = fft(y,m2,1);
if isreal(x) && isreal(y)
    c1 = real(ifft(X.*conj(Y),[],1));
else
    c1 = ifft(X.*conj(Y),[],1);
end
% Keep only the lags we want and move negative lags before positive
% lags.
c = [c1(m2 - mxl + (1:mxl)); c1(1:mxl+1)];

%--------------------------------------------------------------------------

function c = scaleXcorr(scaleType,c,x,y)
% Scale correlation as specified.
coder.internal.prefer_const(scaleType);
slen = length(scaleType);
% Make a quick return if no scaling is needed.
if isempty(scaleType) || strncmpi(scaleType,'none',slen)
    return
end
ySupplied = nargin == 4;
m = size(x,1);
coder.internal.assert(~ySupplied || m == size(y,1), ...
    'signal:xcorr:NoScale','SCALEOPT','none','A','B');
% Perform the requested scaling.
if strncmpi(scaleType,'biased',slen)
    % Scales the raw cross-correlation by 1/M.
    c = c./m;
elseif strncmpi(scaleType,'unbiased',slen);
    % Scales the raw correlation by 1/(M-abs(lags)).
    L = (size(c,1) - 1)/2;
    scaleUnbiased = (m - abs(-L:L)).';
    scaleUnbiased(scaleUnbiased <= 0) = 1;
    c = bsxfun(@rdivide,c,scaleUnbiased);
elseif strncmpi(scaleType,'coeff',slen);
    % Normalizes the sequence so that the auto-correlations
    % at zero lag are identically 1.0.
    if ySupplied
        % Compute autocorrelations at zero lag.
        % scale = norm(x)*norm(y) is numerically superior but slower.
        cxx0 = sum(abs(x).^2);
        cyy0 = sum(abs(y).^2);
        scaleCoeffCross = sqrt(cxx0*cyy0);
        c = c./scaleCoeffCross;
    elseif size(c,2) == 1
        % Autocorrelation of a vector. Normalize by c[0].
        mid = (size(c,1) + 1)/2; % row corresponding to zero lag.
        c = c./c(mid);
    else
        % Compute the indices corresponding to the columns that are
        % autocorrelations.
        n = size(x,2);
        % Note that size(c,2) = n^2.
        kvec = 1:n+1:n*n; % a 1-by-n row vector
        % kvec is an index vector such that for an n-by-n matrix A,
        % A(kvec(j)) = A(j,j).
        mid = (size(c,1) + 1)/2; % row index corresponding to zero lag
        trow = sqrt(c(mid,kvec)); % a 1-by-n row vector
        tmat = trow.'*trow; % an n-by-n matrix, tmat(i,j) = trow(i)*trow(j)
        scaleCoeffAuto = tmat(:).'; % a 1-by-n^2 row vector
        % The autocorrelations at zero-lag are normalized to one.
        c = bsxfun(@rdivide,c,scaleCoeffAuto);
    end
else
    % This case will always error, but if scaleType is not a constant, then
    % we need to have a condition that isn't obviously a constant false to
    % avoid a compile-time error in code-generation.
    coder.internal.assert(strncmpi(scaleType,'none',slen), ...
        'signal:xcorr:UnknInput');
end

%--------------------------------------------------------------------------

function p = needsShifting(x)
% Returns true if the leading non-singleton dimension of x needs shifting
% to the fore.
p = size(x,1) == 1 && ~isscalar(x);
% For code generation we require that the above be a constant in order to
% perform the shifting.
p = p && coder.internal.isConst(p);

%--------------------------------------------------------------------------

function [ySupplied,maxlagIdx,scaleIdx] = sortInputs(varargin)
% Returns a logical value indicating whether varargin{1} is a y input, and
% returns the locations of maxlag and scale in varargin, if present. The
% index values will be zero when that particular type of input was not
% supplied. This routine is designed to be constant-folded when generating
% code.
ySupplied = false;
maxlagIdx = 0;
scaleIdx = 0;
if nargin == 0
    % xcorr(x)
elseif nargin == 1
    if ischar(varargin{1})
        % xcorr(x,scaletype)
        scaleIdx = 1;
    elseif isscalar(varargin{1}) && ...
            coder.internal.isConst(size(varargin{1}))
        % xcorr(x,maxlag)
        maxlagIdx = 1;
    else
        % xcorr(x,y)
        ySupplied = true;
    end
elseif nargin == 2
    if ischar(varargin{1})
        % xcorr(x,scaletype,maxlag)
        scaleIdx = 1;
        maxlagIdx = 2;
    elseif ischar(varargin{2})
        if isscalar(varargin{1}) && ...
                coder.internal.isConst(size(varargin{1}));
            % xcorr(x,maxlag,scaletype)
            maxlagIdx = 1;
            scaleIdx = 2;
        else
            % xcorr(x,y,scaletype)
            ySupplied = true;
            scaleIdx = 2;
        end
    else
        % xcorr(x,y,maxlag)
        ySupplied = true;
        maxlagIdx = 2;
    end
else
    if ischar(varargin{2})
        % xcorr(x,y,scaletype,maxlag)
        ySupplied = true;
        scaleIdx = 2;
        maxlagIdx = 3;
    else
        % xcorr(x,y,maxlag,scaletype)
        ySupplied = true;
        maxlagIdx = 2;
        scaleIdx = 3;
    end
end

%--------------------------------------------------------------------------

function maxlag = fetchMaxlag(maxlagIdx,defaultMaxlag,varargin)
% If maxlag has been supplied, read it from varargin and validate it,
% otherwise return the default value.
coder.internal.prefer_const(maxlagIdx,defaultMaxlag,varargin);
if maxlagIdx == 0 || isempty(varargin{maxlagIdx})
    maxlag = defaultMaxlag;
else
    maxlag = abs(double(varargin{maxlagIdx}(1)));
    % Validate MAXLAG input
    coder.internal.assert(isscalar(varargin{maxlagIdx}), ...
        'signal:xcorr:MaxLagMustBeScalar','MAXLAG');
    coder.internal.assert(isnumeric(varargin{maxlagIdx}), ...
        'signal:xcorr:UnknInput');
    coder.internal.assert(maxlag == floor(maxlag), ...
        'signal:xcorr:MaxLagMustBeInteger','MAXLAG');
end

%--------------------------------------------------------------------------

function c = padOutput(c1,maxlag,defaultMaxlag)
% Pads c1 with zeros if maxlag > defaultMaxlag.
if isempty(coder.target)
    % This is a faster idiom in MATLAB than the one used for code
    % generation below.
    if maxlag > defaultMaxlag
        % Pad with zeros and unshift.
        zeropad = zeros(maxlag - defaultMaxlag,size(c1,2),'like',c1);
        c = [zeropad; c1; zeropad];
    else
        c = c1;
    end
else
    coder.internal.prefer_const(maxlag,defaultMaxlag);
    % MATLAB Coder size inference is helped by being explicit about the
    % relationship between maxlag and the number of rows of c.
    c = zeros(2*maxlag + 1,size(c1,2),'like',c1);
    if maxlag > defaultMaxlag
        % Pad with zeros.
        offset = maxlag - defaultMaxlag;
        for j = 1:size(c,2)
            for i = 1:size(c1,1)
                c(offset + i,j) = c1(i,j);
            end
        end
    else
        c(:,:) = c1(:,:);
    end
end

%--------------------------------------------------------------------------

function c = autocorrTD(x,maxlag)
% Auto-correlation of a column vector using the direct "time domain"
% calculation. The output is trimmed according to maxlag but not padded if
% maxlag >= size(x,1). This function is written for code generation only.
coder.internal.prefer_const(maxlag);
m = size(x,1);
% Even if the caller enforces maxlag < m, it may help size inference in
% code generation to reiterate the fact here.
mxl = min(maxlag,m - 1);
nc = 2*mxl + 1;
if islogical(x)
    czero = 0;
else
    czero = zeros('like',x);
end
c = zeros(nc,1,'like',czero);
% coder.internal.conjtimes(x,y) is used below to evaluate conj(x)*y. It
% tends to produce slightly better generated code in some cases, and it has
% the benefit of handling the case where x is logical.
for k = 0:mxl
    s = czero;
    for i = 1:m - k
        s = s + coder.internal.conjtimes(x(i),x(k + i));
    end
    c(mxl - k + 1) = conj(s);
    c(mxl + k + 1) = s;
end

%--------------------------------------------------------------------------

function c = crosscorrTD(x,y,maxlag)
% Cross-correlation of two column vectors using the direct "time domain"
% calculation. The output is trimmed according to maxlag but not padded if
% maxlag >= max(size(x,1),size(y,1)). This function is written for code
% generation only.
coder.internal.prefer_const(maxlag);
m = size(x,1);
n = size(y,1);
maxmn = max(m,n);
% Even if the caller enforces maxlag < maxmn, it may help size inference in
% code generation to reiterate the fact here.
mxl = min(maxlag,maxmn - 1);
nc = 2*mxl + 1;
if islogical(x)
    xzero = 0;
else
    xzero = zeros('like',x);
end
if islogical(y)
    yzero = 0;
else
    yzero = zeros('like',y);
end
czero = xzero + yzero;
c = zeros(nc,1,'like',czero);
% coder.internal.conjtimes(x,y) is used below to evaluate conj(x)*y. It
% tends to produce slightly better generated code in some cases, and it has
% the benefit of handling the case where x is logical.
for k = 0:mxl
    ihi = min(m - k,n);
    s = czero;
    for i = 1:ihi
        s = s + coder.internal.conjtimes(y(i),x(k + i));
    end
    c(mxl + k + 1) = s;
end
for k = 1:mxl
    ihi = min(m,n - k);
    s = czero;
    for i = 1:ihi
        s = s + coder.internal.conjtimes(y(k + i),x(i));
    end
    c(mxl - k + 1) = s;
end

%--------------------------------------------------------------------------

function c = OpCountForAutocorrTD(maxlag,realp,m)
% Real arithmetic operation count for time domain auto-correlation.
% Assumes maxlag < m.
coder.internal.prefer_const(maxlag,realp,m);
if realp
    c0 = 2*m - 1;
    % k = 1:maxlag
    % Sn = sum(2*(m - k) - 1) = maxlag*c0 - maxlag*(maxlag + 1);
    Sn = maxlag*(c0 - maxlag - 1);
else
    c0 = 8*m - 2;
    % k = 1:maxlag
    % Sn = sum(8*(m - k) - 2) = maxlag*c0 - 4*maxlag*(maxlag + 1);
    Sn = maxlag*(c0 - 4*maxlag - 4);
end
c = c0 + Sn;

%--------------------------------------------------------------------------

function c = OpCountForXcorrTD(maxlag,realp,m1,n1)
% Real arithmetic operation count for time domain cross-correlation. This
% function is only used for code generation. Assumes maxlag < max(m1,n1).
coder.internal.prefer_const(maxlag,realp,m1,n1);
m = max(m1,n1);
n = min(m1,n1);
if realp
    c0 = 2*n - 1;
    if maxlag <= n - 1
        % k = 1:maxlag
        % Sn = sum(2*(n - k) - 1) = maxlag*c0 - maxlag*(maxlag + 1);
        Sn = maxlag*(c0 - maxlag - 1);
        if maxlag <= m - n
            c = c0 + maxlag*c0 + Sn;
        else
            % k = (m - n + 1):maxlag
            % Sm = sum(2*(m - k) - 1);
            Sm = (maxlag - (m - n))*(m - maxlag + n - 2);
            c = c0 + (m - n)*c0 + Sm + Sn;
        end
    elseif maxlag <= m - 1
        % Sn = (n - 1)*c0 - (n - 1)*n = (n - 1)*(c0 - n) = (n - 1).^2.
        Sn = (n - 1).^2;
        if maxlag <= m - n
            c = c0 + maxlag*c0 + Sn;
        else
            Sm = (maxlag - (m - n))*(m - maxlag + n - 2);
            c = c0 + (m - n)*c0 + Sm + Sn;
        end
    else
        c = 2*m*n - (m + n - 1);
    end
else
    c0 = 8*n - 2;
    if maxlag <= n - 1
        % k = 1:maxlag
        % Sn = sum(8*(n - k) - 2) = maxlag*c0 - 4*maxlag*(maxlag + 1);
        Sn = maxlag*(c0 - 4*maxlag - 4);
        if maxlag <= m - n
            c = c0 + maxlag*c0 + Sn;
        else
            % k = (m - n + 1):maxlag
            % Sm = sum(8*(m - k) - 2);
            Sm = (maxlag - (m - n))*(4*(m - maxlag + n) - 6);
            c = c0 + (m - n)*c0 + Sm + Sn;
        end
    elseif maxlag <= m - 1
        % Sn = (n - 1)*c0 - 4*(n - 1)*n
        Sn = (n - 1)*c0 - 4*(n - 1)*n;
        if maxlag <= m - n
            c = c0 + maxlag*c0 + Sn;
        else
            Sm = (maxlag - (m - n))*(4*(m - maxlag + n) - 6);
            c = c0 + (m - n)*c0 + Sm + Sn;
        end
    else
        c = 8*m*n - 2*(m + n - 1);
    end
end

%--------------------------------------------------------------------------

function [c,lags] = zeroOutputs(x,varargin)
% Return zero output arrays of the correct sizes for Simulink's size
% propagation pass.
coder.internal.prefer_const(varargin);
[ySupplied,maxlagIdx] = coder.const(@sortInputs,varargin{:});
dim = coder.internal.constNonSingletonDim(x);
if coder.const(needsShifting(x))
    nshifts = double(dim - 1);
else
    nshifts = 0;
end
m = size(x,dim);
if coder.const(maxlagIdx > 0 && ~isempty(varargin{maxlagIdx}))
    maxlag = abs(double(varargin{maxlagIdx}));
elseif ySupplied
    n = numel(varargin{1});
    maxlag = max(m,n) - 1;
else
    maxlag = m - 1;
end
nrows = 2*maxlag + 1;
ncols = coder.internal.prodsize(x,'above',dim);
lags = zeros(1,nrows);
c1 = zeros(nrows,ncols*ncols);
if nshifts > 0
    c = shiftdim(c1,-nshifts);
else
    c = c1;
end

%--------------------------------------------------------------------------
