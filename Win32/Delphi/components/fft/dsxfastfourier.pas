// Gunnar Bolle, FPrefect@t-online.de
// Simple FFT component
// Feel free to use this code
// Based upon a pascal routine from - Don Cross <dcross@intersrv.com>
// Thanks Don, nice Job.
// If you want to know more about FFT and its theorie visit
// http://www.intersrv.com/~dcross
// Thanks to Kees Huls for providing me the missing author information 
//
// Please note : I didn't have the time to write a sample application for this.
//               Please do not ask me about how to use this one ...
//               If you're aware of FFT you'll know how. Otherwise, try
//               some of those neat Button components at DSP. They're quite easy
//               to handle.


unit DSXFastFourier;

interface

uses
  Windows, Messages, SysUtils, Classes,math;

procedure Register;

type
TComplex = Record
   Real : double;
   imag : double;
end;

TOnGetDataEvent = procedure(index : integer; var Value : TComplex) of Object;

TComplexArray = array [0..0] of TComplex;
PComplexArray = ^TComplexArray;

EFastFourierError = class(Exception);

TDSXFastFourier = Class(TComponent)

private
    FNumSamples    : integer;
    JobDone        : boolean;
    FInBuffer      : PComplexArray;
    FOutBuffer     : PComplexArray;
    FOnGetData     : TOnGetDataEvent;

    function  IsPowerOfTwo ( x: word ): boolean;
    function  NumberOfBitsNeeded ( PowerOfTwo: word ): word;
    function  ReverseBits ( index, NumBits: word ): word;
    procedure FourierTransform ( AngleNumerator:  double );
    procedure SetNumSamples(value : integer);
    function  GetTransformedData(idx : integer) : TComplex;

    constructor
              create(AOwner : TComponent);
    destructor
              destroy;

public
    procedure fft;
    procedure ifft;
    procedure CalcFrequency (FrequencyIndex: word);
    function  NearestPowerOfTwo ( x: word ): word;//ajout personnel: retourne la valeur la plus proche inférieure en poweroftwo.

published
    property  OnGetData   : TOnGetDataEvent read FOnGetData write FOnGetData;
    property  NumSamples  : integer read FNumSamples write SetNumSamples;
    property  SampleCount : Integer read FNumSamples;
    property  TransformedData[idx : integer] : TComplex read GetTransformedData;
    property  JobTerminated : boolean read JobDone write JobDone;
end;

implementation

constructor TDSXFastFourier.Create(AOwner : TComponent);
begin
   inherited create(AOwner);
end;

destructor TDSXFastFourier.Destroy;
begin
  if Assigned(FInBuffer) then
      FreeMem(FinBuffer);
  if Assigned(FOutBuffer) then
      FreeMem(FOutBuffer);
end;


procedure TDSXFastFourier.SetNumSamples(value : integer);
begin

   FNumSamples := value;

   if Assigned(FInBuffer) then
      FreeMem(FinBuffer);

   if Assigned(FOutBuffer) then
      FreeMem(FOutBuffer);

   try
     getMem(FInBuffer, sizeof(TComplex)*FNumSamples);
     getMem(FOutBuffer, sizeof(TComplex)*FNumSamples);
   except on EOutOfMemory do
     raise EFastFourierError.Create('Could not allocate memory for complex arrays');
   end;

end;

function  TDSXFastFourier.GetTransformedData(idx : integer) : TComplex;
begin
  Result := FOutBuffer[idx];
end;

function TDSXFastFourier.IsPowerOfTwo ( x: word ): boolean;
var   i, y:  word;
begin
    y := 2;
    for i := 1 to 31 do begin
        if x = y then begin
            IsPowerOfTwo := TRUE;
            exit;
        end;
        y := y SHL 1;
    end;

    IsPowerOfTwo := FALSE;
end;

function TDSXFastFourier.NearestPowerOfTwo ( x: word ): word;
var   i, y:  word;
begin
    y := 2;
    for i := 1 to 31 do begin
        if x < y then begin
            NearestPowerOfTwo := y div 2;
            exit;
        end;
        y := y SHL 1;
    end;

    NearestPowerOfTwo := y;
end;

function TDSXFastFourier.NumberOfBitsNeeded ( PowerOfTwo: word ): word;
var     i: word;
begin
    for i := 0 to 16 do begin
        if (PowerOfTwo AND (1 SHL i)) <> 0 then begin
            NumberOfBitsNeeded := i;
            exit;
        end;
    end;
end;


function TDSXFastFourier.ReverseBits ( index, NumBits: word ): word;
var     i, rev: word;
begin
    rev := 0;
    for i := 0 to NumBits-1 do begin
        rev := (rev SHL 1) OR (index AND 1);
        index := index SHR 1;
    end;

    ReverseBits := rev;
end;


procedure TDSXFastFourier.FourierTransform ( AngleNumerator:  double);
var
    NumBits, i, j, k, n, BlockSize, BlockEnd: word;
    delta_angle, delta_ar: double;
    alpha, beta: double;
    tr, ti, ar, ai: double;
begin
    if not IsPowerOfTwo(FNumSamples) or (FNumSamples<2) then
        raise EFastFourierError.Create('NumSamples is not a positive integer power of 2');

    if not assigned(FOnGetData) then
       raise EFastFourierError.Create('You must specify an OnGetData handler');

    NumBits := NumberOfBitsNeeded (FNumSamples);
    for i := 0 to FNumSamples-1 do begin
        j := ReverseBits ( i, NumBits );
        FOnGetData(i,FInBuffer[i]);
        FOutBuffer[j] := FInBuffer[i];
    end;
    BlockEnd := 1;
    BlockSize := 2;
    while BlockSize <= FNumSamples do begin
        delta_angle := AngleNumerator / BlockSize;
        alpha := sin ( 0.5 * delta_angle );
        alpha := 2.0 * alpha * alpha;
        beta := sin ( delta_angle );

        i := 0;
        while i < FNumSamples do begin
            ar := 1.0;    (* cos(0) *)
            ai := 0.0;    (* sin(0) *)

            j := i;
            for n := 0 to BlockEnd-1 do begin
                k := j + BlockEnd;
                tr := ar*FOutBuffer[k].Real - ai*FOutBuffer[k].Imag;
                ti := ar*FOutBuffer[k].Imag + ai*FOutBuffer[k].Real;
                FOutBuffer[k].Real := FOutBuffer[j].Real - tr;
                FOutBuffer[k].Imag := FOutBuffer[j].Imag - ti;
                FOutBuffer[j].Real := FOutBuffer[j].Real + tr;
                FOutBuffer[j].Imag := FOutBuffer[j].Imag + ti;
                delta_ar := alpha*ar + beta*ai;
                ai := ai - (alpha*ai - beta*ar);
                ar := ar - delta_ar;
                INC(j);
            end;
            i := i + BlockSize;
        end;
        BlockEnd := BlockSize;
        BlockSize := BlockSize SHL 1;
    end;
end;


procedure TDSXFastFourier.fft;
begin
    FourierTransform ( 2*PI);
    JobDone := True;
end;


procedure TDSXFastFourier.ifft;
var
    i: word;
begin
    FourierTransform ( -2*PI);

    (* Normalize the resulting time samples... *)
    for i := 0 to FNumSamples-1 do begin
        FOutBuffer[i].Real := FOutBuffer[i].Real / FNumSamples;
        FOutBuffer[i].Imag := FOutBuffer[i].Imag / FNumSamples;
    end;
    JobDone := True;
end;


procedure TDSXFastFourier.CalcFrequency (FrequencyIndex: word);
var
    k: word;
    cos1, cos2, cos3, theta, beta: double;
    sin1, sin2, sin3: double;
begin
    FOutBuffer[0].Real := 0.0;
    FOutBuffer[0].Imag := 0.0;
    theta := 2*PI * FrequencyIndex / FNumSamples;
    sin1 := sin ( -2 * theta );
    sin2 := sin ( -theta );
    cos1 := cos ( -2 * theta );
    cos2 := cos ( -theta );
    beta := 2 * cos2;
    for k := 0 to FNumSamples-1 do begin
        sin3 := beta*sin2 - sin1;
        sin1 := sin2;
        sin2 := sin3;
        cos3 := beta*cos2 - cos1;
        cos1 := cos2;
        cos2 := cos3;
        FOutBuffer[0].Real := FOutBuffer[0].Real + FInBuffer[k].Real*cos3 - FInBuffer[k].Imag*sin3;
        FOutBuffer[0].Imag := FOutBuffer[0].Imag + FInBuffer[k].Imag*cos3 + FInBuffer[k].Real*sin3;
    end;
end;

procedure Register;
begin
  RegisterComponents('Components', [TDSXFastFourier]);
end;

end.
