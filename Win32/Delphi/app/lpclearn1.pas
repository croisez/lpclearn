{**********************************************************}
{ LPCLEARN                                                 }
{ Copyright 2000 FAculté Polytechnique de Mons-L.M. Croisez}
{                                                          }
{ This component is FreeWare and can freely be distributed }
{ as long as the source of this component contains the     }
{ the name of the original creator of this component       }
{                                                          }
{ Louis Marie Croisez                                      }
{ croisez@tcts.fpms.ac.be                                  }
{**********************************************************}

unit lpclearn1;

interface

uses
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  AudioIO, ExtCtrls, StdCtrls, Buttons, DSXFastFourier, Math, ComCtrls,
  URLabel, Menus;

type

////////////////////////////////////////////////////////////////////////
  complex      = record Re : double; Im : double; end;
  VComplex     = Array [0..4000] of complex;
  VDouble0     = Array [0..8000] of double;
  VDouble1     = Array [0..1024] of double;//big frame
  VDouble2     = Array [0..128] of double;//little frame
  VDouble3     = Array [0..15] of double; //filter coef
  VSmallInt    = Array [0..1023] of smallint;
  VComplexPtr  = ^VComplex;
  VDouble0Ptr  = ^VDouble0;
  VDouble1Ptr  = ^VDouble1;
  VDouble2Ptr  = ^VDouble2;
  VDouble3Ptr  = ^VDouble3;

  TLPC = class(TComponent)
    procedure razFilterMemories;
    procedure calculate_plot_freqenv;//math & graph part of fft envelope.
    procedure roots1(A : VDouble0Ptr;Order : integer;Roots : VComplexPtr);
    procedure ai2ki(ai : VDouble0Ptr;Order : integer; ki : VDouble0Ptr);
    procedure ki2ai(ki : VDouble0Ptr;Order : integer; ai : VDouble0Ptr);
    procedure unitcircle;     //math part of the unitcircle process.
    procedure drawunitcircle; //graph part of the unitcircle process.
    procedure drawRootsInTheUnitCircle;
    procedure calculate_plot_time_fft;//math & graph part of time & fft of the output of the synthesis filter.
    procedure Filter_IIR_lattice(ki : VDouble0Ptr; DenOrder : integer; x : VDouble0Ptr; NPoint : integer; y : VDouble0Ptr; zi : VDouble0Ptr; SIGMA : double);
    procedure hamming(Ham: VDouble0Ptr; nb: Integer);
    Procedure Autocorr(RealSignal : VDouble0Ptr; NPoints : integer; Atcr : VDouble0Ptr; MaxOrder : Integer);
    function  Schur(Autocorr :VDouble0Ptr;Order : integer; Parcor :VDouble0Ptr): Double;
    procedure plotTubemodel;
    procedure updateCoefLabels;
  private
  public
  end;

////////////////////////////////////////////////////////////////////////

  TForm1 = class(TForm)
    wave: TPaintBox;
    spectrum: TPaintBox;
    Label3: TLabel;
    DSXFastFourier1: TDSXFastFourier;
    StatusBar1: TStatusBar;
    Image1: TImage;
    Label1: TLabel;
    Label4: TLabel;
    unitcircle: TPaintBox;
    Label6: TLabel;
    PageControl2: TPageControl;
    TabSheet5: TTabSheet;
    Label11: TLabel;
    Label10: TLabel;
    Label9: TLabel;
    Label8: TLabel;
    Label12: TLabel;
    Label13: TLabel;
    Label14: TLabel;
    Label15: TLabel;
    Label16: TLabel;
    Label17: TLabel;
    K10: TScrollBar;
    K9: TScrollBar;
    K8: TScrollBar;
    K7: TScrollBar;
    K6: TScrollBar;
    K5: TScrollBar;
    K4: TScrollBar;
    K3: TScrollBar;
    K2: TScrollBar;
    K1: TScrollBar;
    TabSheet6: TTabSheet;
    Label18: TLabel;
    Label19: TLabel;
    Label20: TLabel;
    Label21: TLabel;
    Label22: TLabel;
    Label23: TLabel;
    Label24: TLabel;
    Label25: TLabel;
    Label26: TLabel;
    Label27: TLabel;
    Label28: TLabel;
    Label29: TLabel;
    Label30: TLabel;
    A10: TScrollBar;
    A9: TScrollBar;
    A8: TScrollBar;
    A7: TScrollBar;
    A6: TScrollBar;
    A5: TScrollBar;
    A4: TScrollBar;
    A3: TScrollBar;
    A2: TScrollBar;
    A1: TScrollBar;
    A0: TScrollBar;
    aboutbox: TGroupBox;
    Image3: TImage;
    Memo1: TMemo;
    Button1: TButton;
    Memo2: TMemo;
    amplitude: TScrollBar;
    Label31: TLabel;
    AGC: TCheckBox;
    PlayButton: TSpeedButton;
    RecButton: TSpeedButton;
    unvoiced: TRadioButton;
    voiced: TRadioButton;
    Label32: TLabel;
    pitch: TScrollBar;
    Label33: TLabel;
    MS: TButton;
    MR: TButton;
    Label34: TLabel;
    URLabel1: TURLabel;
    SpeedButton1: TSpeedButton;
    v_uv: TPaintBox;
    x2: TCheckBox;
    tubemodel: TPaintBox;
    Label2: TLabel;
    Label35: TLabel;
    AudioOut: TAudioOut;
    AudioIn: TAudioIn;
    SaveMenu: TPopupMenu;
    Savetomemory1: TMenuItem;
    Savetofile1: TMenuItem;
    loadMenu: TPopupMenu;
    Loadfrommemory1: TMenuItem;
    Loadfromfile1: TMenuItem;
    procedure FormCreate(Sender: TObject);
    procedure modelChoice(voiced: boolean);
    procedure DSXFastFourier1GetData(index: Integer; var Value: TComplex);
    procedure spectrumMouseMove(Sender: TObject; Shift: TShiftState; X,
      Y: Integer);
    procedure waveMouseMove(Sender: TObject; Shift: TShiftState; X,
      Y: Integer);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure Button1Click(Sender: TObject);
    procedure pitchChange(Sender: TObject);
    procedure K1Change(Sender: TObject);
    procedure K2Change(Sender: TObject);
    procedure K3Change(Sender: TObject);
    procedure K4Change(Sender: TObject);
    procedure K5Change(Sender: TObject);
    procedure K6Change(Sender: TObject);
    procedure K7Change(Sender: TObject);
    procedure K8Change(Sender: TObject);
    procedure K9Change(Sender: TObject);
    procedure K10Change(Sender: TObject);
    procedure A0Change(Sender: TObject);
    procedure A1Change(Sender: TObject);
    procedure A2Change(Sender: TObject);
    procedure A3Change(Sender: TObject);
    procedure A4Change(Sender: TObject);
    procedure A5Change(Sender: TObject);
    procedure A6Change(Sender: TObject);
    procedure A7Change(Sender: TObject);
    procedure A8Change(Sender: TObject);
    procedure A9Change(Sender: TObject);
    procedure A10Change(Sender: TObject);
    procedure amplitudeChange(Sender: TObject);
    procedure voicedClick(Sender: TObject);
    procedure unvoicedClick(Sender: TObject);
    procedure TabSheet5MouseDown(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure PageControl2Change(Sender: TObject);
    procedure RecButtonClick(Sender: TObject);
    procedure FormShow(Sender: TObject);
    procedure PageControl2Changing(Sender: TObject;
      var AllowChange: Boolean);
    procedure SpeedButton1Click(Sender: TObject);
    procedure PlayButtonClick(Sender: TObject);
    procedure tubemodelMouseMove(Sender: TObject; Shift: TShiftState; X,
      Y: Integer);
    function AudioOutFillBuffer(Buffer: PChar;
      var Size: Integer): Boolean;
    function AudioInBufferFilled(Buffer: PChar;
      var Size: Integer): Boolean;
    procedure PlayStop;
    procedure PlayStart;
    procedure RecordStop;
    procedure RecordStart;
    procedure AudioOutStop(Sender: TObject);
    procedure AudioOutStart(Sender: TObject);
    procedure AudioInStart(Sender: TObject);
    procedure AudioInStop(Sender: TObject);
    procedure Savetofile1Click(Sender: TObject);
    procedure Savetomemory1Click(Sender: TObject);
    procedure Loadfrommemory1Click(Sender: TObject);
    procedure Loadfromfile1Click(Sender: TObject);
    procedure MSClick(Sender: TObject);
    procedure MRClick(Sender: TObject);
  private
  public
  end;
////////////////////////////////////////////////////////////////////////
var
  //variables globales.
  Form1: TForm1;
  waveValue: VDouble0;
  Fe: integer;

  //variables propres au modèle LPC
  LPC: TLPC;

  roots: VComplex;
  pRoots: VComplexPtr;

  y2: VDouble0; //1024 samples

  x, x_env, y, y_env : VDouble0;
  px, px_env, py, py_env : VDouble0Ptr;

  Atcr, ki, ai, zi, kimem : VDouble0;
  pAtcr, pki, pai, pzi, pkimem : VDouble0Ptr;

  NOrder: integer;
  SIGMA, sigmaMem: double;
  PitchVal: double;
  iphase, fft_offset, nCalculateSchur: integer;
  bVoiced : boolean;

  bAudioInStarted, bAudioOutStarted: boolean;
  FrameSize: integer;

implementation

{$R *.DFM}

/////////////////////////////////////////////////////////////////////////
procedure TLPC.calculate_plot_time_fft;
var i, j, xs, ys, N, xi: integer;
begin
  with form1 do
  begin
   N := DSXFastFourier1.NumSamples;
   for i:=0 to N-1 do
   begin
     waveValue[i]:=y2[i];
   end;

   xs:=wave.width ;
   ys:=wave.height;
   wave.canvas.pen.Color:=clBlack;
   wave.canvas.brush.color:=clYellow;
   wave.canvas.CopyMode:=cmSrcCopy;
   wave.canvas.FillRect(Rect(0,0,xs,ys));
   wave.canvas.MoveTo(0,ys div 2);
   For j := 0 to (xs)-1 Do wave.canvas.LineTo(j,ys div 2-round(waveValue[j]*ys*0.4));

   //Calcul de la FFT
   DSXFastFourier1.JobTerminated := False;
   DSXFastFourier1.fft;
   While (not DSXFastFourier1.JobTerminated) do;

   xs:=spectrum.width ;
   ys:=spectrum.height;
   spectrum.canvas.brush.color:=clWindow;
   spectrum.canvas.CopyMode:=cmSrcCopy;
   spectrum.canvas.FillRect(Rect(0,0,xs,ys));

   //Calcul de la norme (sur toutes les valeurs de la FFT)
   For j := 0 to (N div 2)-1 Do //Calcul de la norme
       waveValue[j] := Sqrt(DSXFastFourier1.TransformedData[j].Real * DSXFastFourier1.TransformedData[j].Real + DSXFastFourier1.TransformedData[j].imag * DSXFastFourier1.TransformedData[j].imag);
   //Mise à zero du reste du buffer d'entiers.
   For j := (N div 2) to N Do
       waveValue[j] := 0;

   //FFT plot
   spectrum.canvas.pen.Color:=clblue;
   if waveValue[0]<>0 then xi := round( ys/6*ln(waveValue[0]) ) else xi := 0;
   spectrum.canvas.MoveTo(0,ys-xi);
   For j := 0 to (N div 2)-1 Do
   begin
      if waveValue[j]<>0 then xi := round( ys/6*ln(waveValue[j]) ) else xi := 0;
      spectrum.canvas.moveto(j,ys);
      spectrum.canvas.LineTo(j,ys-xi);
   end;

//   LPC.calculate_plot_freqenv;
 end;
end;

procedure TLPC.Filter_IIR_lattice(
                                  ki : VDouble0Ptr;
                                  DenOrder: integer;
                                  x : VDouble0Ptr;
                                  NPoint : integer;
                                  y : VDouble0Ptr;
				  zi : VDouble0Ptr;
                                  SIGMA: double
                                  );
{	Computes the output y of an IIR digital filter when
{	signal x (length=NPoints) is presented at the input and the
{	filter coefficients are given in their PARCOR form.
{	Its transfer function is :
{
{		        		1
{	T(z)= SIGMA *   ----------------------------------------------------
{			1   +  A[1] z^-1   + ... +  A[DenOrder] z^-DenOrder
{
{	where B(z) is the polynomial whose PARCOR form is given
{	in vector ki, in the order k(1),..., k(NumOrder).
{	A 2-Multiplier cell cascade structure is implemented.
{	ZI is the vector of internal variables of the filter.
{	It should have a dimension = DenOrder.
{	It is set to the final conditions on return.
}
var
	i,j : integer;
	y2 : VDouble0Ptr;
	x1,y1 : double;
begin
GetMem(y2,(DenOrder)*sizeof(double));
for i:= 0 to NPoint-1 do
	begin
	x1:=x^[i];
	for j:=0 to DenOrder-1 do
		begin {2-M cell : upper part}
		x1    :=x1-ki^[j]*zi^[j];
		y2^[j]:=x1;
		end;
	y^[i]:= SIGMA * x1;
	for j:=DenOrder-1 downto 1 do
		begin {2-M cell : lower part}
		y1    :=zi^[j]+ki^[j]*y2^[j];
		zi^[j]:=x1;
		x1    :=y1;
		end;
	zi^[0]:=x1;{first cell}
	end;
FreeMem(y2,(DenOrder)*sizeof(double));
end;

procedure TLPC.drawunitcircle;
var v,xs,ys,i, xc, yc, R: integer;
begin
  with form1.unitcircle do
  begin
    xs:=width ;
    ys:=height;
    canvas.CopyMode:=cmSrcCopy;
    canvas.brush.color:=clsilver;
    canvas.FillRect(Rect(0,0,xs,ys));
    xc:=xs div 2;
    yc:=ys div 2;
    R:=(6*xc) div 8;
    v:=20;

    canvas.pen.color:=clblack;
    canvas.moveto(xc,v-5);  canvas.lineto(xc,ys-v);
    canvas.moveto(v-5, yc); canvas.lineto(xs-v,yc);
    canvas.Font.Color := clBlack;
    canvas.textout(xc,ys-v,'-1');
    canvas.textout(xc,v-5,'+1   RE');
    canvas.textout(v-5,yc,'-1');
    canvas.textout(xs-v,yc,'+1   IM');

    canvas.pen.color:=clblack;
    canvas.moveto(xc+R,yc);
    for i:=0 to 359 do
    begin
      canvas.lineto(xc+round(R*cos(i/180*pi)),yc+round(R*sin(i/180*pi)));
    end;
  end;
end;

procedure TLPC.drawRootsInTheUnitCircle;
var xs,ys,i, xc, yc, R: integer;
    xroot, yroot, droot: double;
begin
  with form1.unitcircle do
  begin
    xs:=width ;
    ys:=height;
    xc:=xs div 2;
    yc:=ys div 2;
    R:=(6*xc) div 8;

    canvas.Font.Color := clRed;
    for i:=0 to NOrder-1 do
    begin
      //put (1/roots.re;-roots.im) on the unit circle.
      //takes 1/RE and -IM to represent the roots of 1/Ap(z) and not the roots of Ap(z).
      droot :=  roots[i].Re*roots[i].Re + roots[i].Im*roots[i].Im;
      xroot :=  roots[i].Re/droot;
      yroot := -roots[i].Im/droot;
      canvas.textout(xc-3 + round(R*xroot), yc-6 - round(R*yroot), 'X');
    end;

  end;
end;

procedure TLPC.unitcircle;
begin
  form1.label6.caption:='';
  try
    LPC.roots1(pai, NOrder, pRoots);
  except
    on E: EOverflow do form1.label6.caption:=E.message;
    on E: EInvalidOp do form1.label6.caption:=E.message;
  else
    form1.label6.caption:='Unknown exception';
  end;

  LPC.plotTubemodel;
  LPC.drawunitcircle;
  LPC.drawRootsInTheUnitCircle;
end;

procedure TLPC.ai2ki(ai : VDouble0Ptr;Order : integer; ki : VDouble0Ptr);
{	Prediction Coefficients-to-Parcor transformation.
{	Computes PARCOR coefficients ki(0..Order-1) , from the
{	Prediction coefficients ai(0..Order).
}
var 
	m,i  : integer;
	aint : VDouble0Ptr;
begin
      form1.label6.caption:='';
      try

      	GetMem(aint,(Order+1)*SizeOf(double));
	for i:=0 to Order-1 do ki^[i]:=ai^[i+1];
	for m:=Order downto 1 do
		begin
		for i:= 1 to m-1 do
			aint^[i]:=(ki^[i-1]-ki^[m-1]*ki^[m-i-1])/(1-ki^[m-1]*ki^[m-1]);
		for i:=1 to m-1 do ki^[i-1]:=aint^[i];
		end;
	FreeMem(aint,(Order+1)*SizeOf(double));

      except
        on E: EOverflow do form1.label6.caption:=E.message;
        on E: EInvalidOp do form1.label6.caption:=E.message;
      else
        form1.label6.caption:='Unknown exception';
      end;
end;

procedure TLPC.ki2ai(ki : VDouble0Ptr;Order : integer; ai : VDouble0Ptr);
{	Parcor-To-Prediction Coefficients transformation.
{	Computes the Prediction coefficients ai(0..Order) from the
{	PARCOR coefficients ki(0..Order-1).
}
var
	m,i : integer;
	aint : VDouble0Ptr;
begin
      form1.label6.caption:='';
      try

	GetMem(aint,(Order+1)*SizeOf(double));
	aint^[0]:=1;
	FOR i:= 1 TO Order DO aint^[i]:=0;
	for m:= 1 to Order do
		begin
		ai^[0]:=1;
		for i:=1 to m-1 do ai^[i]:=aint^[i]+ki^[m-1]*aint^[m-i];
		ai^[m]:=ki^[m-1];
		for i:=1 to Order do aint^[i]:=ai^[i];
		end;
	FreeMem(aint,(Order+1)*SizeOf(double));

      except
        on E: EOverflow do form1.label6.caption:=E.message;
        on E: EInvalidOp do form1.label6.caption:=E.message;
      else
        form1.label6.caption:='Unknown exception';
      end;
end;

procedure TLPC.roots1(A: VDouble0Ptr; Order: integer; Roots: VComplexPtr);
{	Computes the complex roots of a real coefficients polynomial
{		A(x)=A[0]+A[1] x[1]+...+A[Order] x[i]^Order
{	method : LAGUERRE.
}
var
	I,K	: integer;
	DF		: double;
	F,X,XP,Z	: complex;
	H,Z1,Z2,U,V,W	: complex;
        //Z0 : complex;
	K1,K2	: complex;
	A1,A2	: VDouble0Ptr;

        procedure div_C(Complex1,Complex2 : complex;var Result:complex);
        {	Returns the complex division of two complex numbers :
        {		Result = Complex1 / Complex2
        }
        var
        	temp	: double;
                        bOverflow: boolean;
        begin
                temp:=0; bOverflow:=false;
                try
               	  temp     := Complex2.Re*Complex2.Re+Complex2.Im*Complex2.Im;
                except
                  on E: EOverflow do
                  begin
                    bOverflow:=true;
                    form1.label6.caption := E.Message;
                  end;
                end;

                if (temp<>0) and (not bOverflow) then
                begin
	          Result.Re:=(Complex1.Re*Complex2.Re+Complex1.Im*Complex2.Im)/temp;
        	  Result.Im:=(Complex1.Im*Complex2.Re-Complex1.Re*Complex2.Im)/temp;
                end;

                if (temp=0) or (bOverflow) then
                begin
        	  Result.Re:=0.0;
        	  Result.Im:=0.0;
                end;
        end;

        procedure  sqr_C(ComplexNumber : complex;var Result : complex);
        {	Returns the complex square of ComplexNumber.
        {	ComplexNumber may be the same adress as Result.
        }
        begin
        With ComplexNumber do
        	begin
        	Result.Re:=Re*Re-Im*Im;
        	Result.Im:=2*Re*Im;
        	end;
        end;

        procedure  polyval_C(A : VDouble0Ptr; Order : integer; x : complex; var y : complex);
        {	Computes the complex value of a real coefficients polynomial:
        {		y=A[0]+A[1] x+...+A[Order] x^Order
        {	method : HORNER.
        }
        var
        	i		: integer;
        	temp	: double;
        begin
        	y.Re:=A^[Order];
        	y.Im:=0;
        	for i:=1 to Order do
        		begin
        		temp:=y.Re*x.Re-y.Im*x.Im+A^[Order-i];
        		y.Im:=y.Re*x.Im+y.Im*x.Re;
        		y.Re:=temp;
        		end
        end;

        procedure mult_C(Complex1,Complex2 : complex;var Result:complex);
        {
        {	Returns the product of two complex numbers :
        {
        {		Result = Complex1 * Complex2
        }
        begin
        	Result.Re:=Complex1.Re*Complex2.Re-Complex1.Im*Complex2.Im;
        	Result.Im:=Complex2.Re*Complex1.Im+Complex1.Re*Complex2.Im;
        end;

        function abs_C(ComplexNumber : complex):double;
        {	Computes the complex magnitude of ComplexNumber
        }
        begin
        	abs_C:=SQRT(SQR(ComplexNumber.Re)+SQR(ComplexNumber.Im));
        end;

        function angle_C(ComplexNumber : Complex):double;
        {	Returns the phase angle of ComplexNumber, between -PI and +PI.
        }
        var
        	temp	: double;
        begin
        	If ComplexNumber.Re=0 then
        		If (ComplexNumber.Im<0) then angle_C:=-PI/2
        			else 	If (ComplexNumber.Im>0)	then angle_C:=PI/2
        				else angle_C:=0
        	else
        		begin
        		temp:=ArcTan(ComplexNumber.Im/ComplexNumber.Re);
        		if (ComplexNumber.Re<0) then
        			if (ComplexNumber.Im<0)  then temp:=-pi+temp
        				else temp:=+pi+temp;
        		angle_C:=temp;
        		end;
        end;

        procedure sqrt_C(ComplexNumber : complex;var Result : complex);
        {	Returns the complex square root of ComplexNumber.
        {	ComplexNumber may be the same adress as Result.
        }
        var
        	mag,ang	: double;
        begin
        	mag:=sqrt(abs_C  (ComplexNumber));
        	ang:=angle_C(ComplexNumber)/2;
        	Result.Re:=mag*cos(ang);
        	Result.Im:=mag*sin(ang);
        end;

        procedure rac1(Roots : VComplexPtr; N : integer; x : complex; var Sum : complex);
        {sum (1/(p-pi)}
        var
        	I : integer;
        	V,temp1,temp2	: complex;
        begin
           V.Re:=1.0;
           V.Im:=0.0;
           Sum.Re:=0;Sum.Im:=0;
           for I:=0 to N-1 do
           begin
		temp1.Re:=x.Re-Roots^[I].Re;
		temp1.Im:=x.Im-Roots^[I].Im;
		div_C(V,temp1,temp2);
		Sum.Re:=Sum.Re+temp2.Re;
		Sum.Im:=Sum.Im+temp2.Im;
           end
        end;

        procedure rac2(Roots : VComplexPtr; N : integer; x : complex; var Sum : complex);
        {sum (1/(p-pi)ý}
        var
        	I : integer;
        	V, temp1,temp2	: complex;
        begin
                V.Re:=1.0;
                V.Im:=0.0;
        	Sum.Re:=0;Sum.Im:=0;
        	for I:=0 to N-1 do
        	begin
        		temp1.Re:=x.Re-Roots^[I].Re;
        		temp1.Im:=x.Im-Roots^[I].Im;
        		div_C(V,temp1,temp2);
        		sqr_C(temp2,temp1);
        		Sum.Re:=Sum.Re+temp1.Re;
        		Sum.Im:=Sum.Im+temp1.Im;
        	end
        end;

begin
	GetMem(A1,Order*sizeof(double));
	GetMem(A2,Order*sizeof(double));
	for I:=1 to Order do A1^[I-1]:=I*A^[I];
	for I:=2 to Order do A2^[I-2]:=I*(I-1)*A^[I];
	K:=0;
	while (K<Order) do
		begin
		X.Re:=1;X.Im:=1;
		DF:=1; (* Initialisation pour entrer dans la boucle *)
		while (DF>=Order*1E-06) do
			begin
			polyval_C(A,Order,X,F);
			polyval_C(A1,Order-1,X,K1);
			rac1(Roots,K,X,Z1);
			mult_C(F,Z1,U);
			V.Re:=K1.Re;V.Im:=K1.Im;
			K1.Re:=V.Re-U.Re;K1.Im:=V.Im-U.Im;
			polyval_C(A2,Order-2,X,K2);
			rac2(Roots,K,X,Z2);
			mult_C(Z1,V,U);
			K2.Re:=K2.Re-2*U.Re;K2.Im:=K2.Im-2*U.Im;
			mult_C(F,Z2,U);
			K2.Re:=K2.Re+U.Re;K2.Im:=K2.Im+U.Im;
			sqr_C(Z1,V);
			mult_C(V,F,U);
			K2.Re:=K2.Re+U.Re;K2.Im:=K2.Im+U.Im;
			H.Re:=(Order-1)*(Order-1)*(K1.Re*K1.Re-K1.Im*K1.Im)
					-Order*(Order-1)*(F.Re*K2.Re-F.Im*K2.Im);
			H.Im:=(Order-1)*(Order-1)*2*K1.Re*K1.Im
					-Order*(Order-1)*(F.Re*K2.Im+F.Im*K2.Re);
			sqrt_C(H,H);
			XP.Re:=K1.Re+H.Re;XP.Im:=K1.Im+H.Im;
			W.Re :=K1.Re-H.Re;W.Im :=K1.Im-H.Im;
			if ((W.Re*W.Re+W.Im*W.Im)>(XP.Re*XP.Re+XP.Im*XP.Im)) then
				begin XP.Re:=W.Re;XP.Im:=W.Im;end;
			div_C(F,XP,Z);
			X.Re:=X.Re-Order*Z.Re;X.Im:=X.Im-Order*Z.Im;
			DF:=sqrt(Z.Re*Z.Re+Z.Im*Z.Im);
			end;
		Roots^[K].Re:=X.Re;Roots^[K].Im:=X.Im;
		if (abs(X.Im)>1.e-10) then
			begin inc(K);Roots^[K].Re:=X.Re;Roots^[K].Im:=-X.Im;end;
		inc(K);
		end;
	FreeMem(A1,Order*sizeof(double));
	FreeMem(A2,Order*sizeof(double));
end;

procedure TLPC.calculate_plot_freqenv;
var i, j, ys, N, xi: integer;
begin
  with form1 do
  begin
   N:=DSXFastFourier1.NumSamples;
   for i:=0 to Norder do waveValue[i] := ai[i];
   for i:=NOrder+1 to N do waveValue[i] := 0.0;

   //Calcul de la FFT
   DSXFastFourier1.JobTerminated := False;
   DSXFastFourier1.fft;
   While (not DSXFastFourier1.JobTerminated) do;

   ys:=spectrum.height;
   spectrum.canvas.brush.color:=clWindow;
   spectrum.canvas.CopyMode:=cmSrcCopy;
//   spectrum.canvas.FillRect(Rect(0,0,xs,ys));

   //Calcul de la norme (sur toutes les valeurs de la FFT)
   For j := 0 to (N div 2)-1 Do //Calcul de la norme
       waveValue[j] := Sqrt(DSXFastFourier1.TransformedData[j].Real * DSXFastFourier1.TransformedData[j].Real + DSXFastFourier1.TransformedData[j].imag * DSXFastFourier1.TransformedData[j].imag);
   //Mise à zero du reste du buffer d'entiers.
   For j := (N div 2) to N Do
       waveValue[j] := 0;

   //FFT plot
   spectrum.canvas.pen.Color:=clRed;
//   waveValue[0] := 1/waveValue[0];
   if waveValue[0]<>0 then xi := round(ln(waveValue[0])*ys/6) else xi := 0;
   spectrum.canvas.MoveTo(0,ys - xi);
   For j := 0 to (N div 2)-1 Do
   begin
//     waveValue[j] := 1/waveValue[j];
     if waveValue[j]<>0 then xi := round(ln(waveValue[j])*ys/6) else xi := 0;
//     envValue[j] := trunc(waveValue[j]*ys/6);
     spectrum.canvas.LineTo(j,ys - xi);
   end;
  end;
end;

procedure TLPC.hamming(Ham: VDouble0Ptr; nb: Integer );
{	Stores an N points Hamming Window in Ham
{	not MATLAB compatible (MATLAB=error!).
{	REF = Harris, Proc. IEEE ASSP 78, vol 66, p.62.
}
var i: Integer;
Begin
  For i:=0 To (nb-1) Do
    Ham^[I] := Ham^[I] * (0.54-0.46*COS(2*pi*i/nb));
End;

Procedure TLPC.Autocorr(RealSignal : VDouble0Ptr; NPoints : integer; Atcr : VDouble0Ptr; MaxOrder : Integer);
{	Returns the raw autocorrelation sequence of the NPoints
{	elements of RealSignal, up to and comprising order MaxOrder.
{	Atcr will be filled with MaxOrder+1 elements, with a maximum
{	of NPoints elements.
{	MATLAB compatibility : with "xcorr(x)" but MaxOrder is added
}
Var
	i,j 	: word;
begin
for i:= 0 to MaxOrder do
	begin
	Atcr^[i]:=0;
	for j:=0 to (NPoints-i-1) do
		Atcr^[i]:=Atcr^[i]+RealSignal^[j]*RealSignal^[j+i];
	end;
end;

function TLPC.Schur(Autocorr :VDouble0Ptr;Order : integer; Parcor :VDouble0Ptr): Double;
{	SCHUR Algorithm.
{	Computes PARCOR coefficients PARCOR[0..Order-1] , from the
{	autocorrelation sequence of order Order (Order+1 elements)
{	stored in Autocorr.
{	The Residual energy error is returned by the function.
}
Var
	I,J,K : Integer;
	U,V : VDouble0Ptr;
Begin
	GetMem(U,(Order+1)*SizeOf(Double));
	GetMem(V,(Order+1)*SizeOf(Double));
	If Autocorr^[0]=0 Then Autocorr^[0]:=0.000001;
	U^[Order]:=Autocorr^[Order];
	For I:=1 TO Order Do
        Begin
          V^[I-1]:=Autocorr^[I];
          U^[I-1]:=Autocorr^[I-1]
        End;
	For I:=1 To Order Do
        Begin
	  K:=Order-I;
	  PARCOR^[Order-I]:=-V^[0]/U^[0];
	  U^[0]:=U^[0]+PARCOR^[Order-I]*V^[0];
	  For J:=1 To K Do
	  Begin
	    V^[J-1]:=PARCOR^[Order-I]*U^[J]+V^[J];
	    U^[J]:=U^[J]+PARCOR^[Order-I]*V^[J]
          End
        End;
	Schur:=U^[0];
	FreeMem(U,(Order+1)*SizeOf(Double));
	FreeMem(V,(Order+1)*SizeOf(Double));
End;

procedure TLPC.razFilterMemories;
var i:integer;
begin
  for i:= 0 to NOrder do zi[i]:=0;//Raz filter memories
end;

procedure TLPC.plotTubemodel;
var i, xs, ys :integer;
    Smoins, Splus: double;
begin
  with form1.tubemodel do
  begin
    xs := width;
    ys := height;
    canvas.pen.Color:=clBlack;
    canvas.brush.color:=clAqua;
    canvas.CopyMode:=cmSrcCopy;
    canvas.FillRect(Rect(0,0,xs,ys));
    Smoins := 2;
    canvas.moveto(0, ys - round(10*Smoins));
    canvas.lineto(10, ys - round(10*Smoins));
    for i:=0 to NOrder-1 do
    begin
      if ki[i]<>-1 then
      begin
        Splus := (1-ki[i])*Smoins/(1+ki[i]);
        //plot the surface-line of tube i
        canvas.moveto( (i+1)*10   , ys - round(10*Smoins) );
        canvas.lineto( (i+1)*10   , ys - round(10*Splus)  );
        canvas.lineto( (i+1)*10+10, ys - round(10*Splus)  );
        Smoins:=Splus;
      end;
    end;
  end;
end;

procedure TLPC.updateCoefLabels;
begin
  with form1 do
  begin
    label31.caption := format('Gain value: %f',[SIGMA]);
    label17.caption := format('K1: %f',[ki[0]]);
    label16.caption := format('K2: %f',[ki[1]]);
    label15.caption := format('K3: %f',[ki[2]]);
    label14.caption := format('K4: %f',[ki[3]]);
    label13.caption := format('K5: %f',[ki[4]]);
    label12.caption := format('K6: %f',[ki[5]]);
    label8.caption :=  format('K7: %f',[ki[6]]);
    label9.caption :=  format('K8: %f',[ki[7]]);
    label10.caption := format('K9: %f',[ki[8]]);
    label11.caption := format('K10: %f',[ki[9]]);
    label28.caption := format('A0: %f',[ai[0]]);
    label27.caption := format('A1: %f',[ai[1]]);
    label26.caption := format('A2: %f',[ai[2]]);
    label25.caption := format('A3: %f',[ai[3]]);
    label24.caption := format('A4: %f',[ai[4]]);  
    label23.caption := format('A5: %f',[ai[5]]);
    label22.caption := format('A6: %f',[ai[6]]);
    label21.caption := format('A7: %f',[ai[7]]);
    label20.caption := format('A8: %f',[ai[8]]);
    label19.caption := format('A9: %f',[ai[9]]);
    label18.caption := format('A10: %f',[ai[10]]);
  end;
end;

/////////////////////////////////////////////////////////////////////////

procedure TForm1.FormCreate(Sender: TObject);
var i: integer;
begin
  FrameSize:=500;
  if ParamCount <> 0 then
  begin
    FrameSize := strtoint(ParamStr(1));
    form1.caption := form1.caption + '  -  Framesize=' + paramstr(1);
  end;

  AudioOut.BufferSize := 2 * FrameSize;
  AudioIn.BufferSize := 2 * FrameSize;

  bAudioInStarted := false;
  bAudioOutStarted := false;

  //Initialization of frame process (mmtimer)
  AboutBox.left := 520;
  Aboutbox.top := 304;
  bVoiced:=true;
  iphase := 0;
  fft_offset := 0;
  nCalculateSchur:=0;
  form1.top:=0;
  form1.Left :=0;
  form1.Height := 600;
  form1.Width :=800;
  Fe:=8000;
  //initialisation of the variables of the LPC model
  NOrder := 10;

  pRoots := @roots;
  px:=@x; py:=@y;
  pAtcr:=@Atcr; pki:=@ki; pai:=@ai;
  px_env:=@x_env; py_env:=@y_env; pzi:=@zi; pkimem:=@kimem;

  for i:=0 to 19 do
  begin
    roots[i].Re := 0.0; roots[i].Im := 0.0;
  end;
  for i:=0 to NOrder do
  begin
    Atcr[i]:=0.0; ki[i]:=0.0; ai[i]:=0.0; zi[i]:=0.0; kimem[i]:=0.0;
  end;
  ki[9]:=-0.1;
  for i:=0 to 7999 do
  begin
    x[i]:=0.0; y[i]:=0.0; x_env[i]:=0.0; y_env[i]:=0.0;
  end;
end;

procedure TForm1.DSXFastFourier1GetData(index: Integer;
  var Value: TComplex);
begin
   Value.Real := waveValue[index];
   Value.imag := 0;
end;

procedure TForm1.spectrumMouseMove(Sender: TObject; Shift: TShiftState; X,
  Y: Integer);
var i:integer;
begin
    i:= round((X)/(DSXFastFourier1.NumSamples div 2)*Fe/2);
    Label3.caption := Format('FFT: %d Hz',[i]);
end;

procedure TForm1.waveMouseMove(Sender: TObject; Shift: TShiftState; X,
  Y: Integer);
begin
    Label1.caption := Format('Time: %f ms',[X*Fe/1000]);
end;

procedure TForm1.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  if not aboutbox.visible then
  begin
    if not RecButton.down then
      PlayStop
    else
      RecordStop;
  end;
end;

procedure TForm1.modelChoice(voiced: boolean);
var xs, ys: integer;
begin
  xs:=v_uv.width; ys:=v_uv.height;
  v_uv.canvas.CopyMode:=cmSrcCopy;
  v_uv.canvas.brush.color:=clSilver;
  v_uv.canvas.FillRect(Rect(0,0,xs,ys));
  v_uv.canvas.pen.Color:=clBlack;
  v_uv.Canvas.MoveTo(xs,ys div 2);
  if voiced then v_uv.Canvas.LineTo(0,0) else v_uv.Canvas.LineTo(0,ys);
end;

procedure TForm1.Button1Click(Sender: TObject);
begin
  aboutbox.visible := false;
  LPC.drawunitcircle;
  LPC.plotTubemodel;
  modelChoice(bVOICED);
  PlayButton.down := true;
  PlayButtonClick(Self);
end;

procedure TForm1.pitchChange(Sender: TObject);
begin
  voiced.checked := true;
  PitchVal:=pitch.position/100;
  label33.caption := format('Pitch value: %f (Hz)',[PitchVal]);
end;

procedure TForm1.K1Change(Sender: TObject);
begin
  ki[0]:=K1.position/100;
  label17.caption := format('K1: %f',[ki[0]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure TForm1.K2Change(Sender: TObject);
begin
  ki[1]:=K2.position/100;
  label16.caption := format('K2: %f',[ki[1]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure TForm1.K3Change(Sender: TObject);
begin
  ki[2]:=K3.position/100;
  label15.caption := format('K3: %f',[ki[2]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K4Change(Sender: TObject);
begin
  ki[3]:=K4.position/100;
  label14.caption := format('K4: %f',[ki[3]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K5Change(Sender: TObject);
begin
  ki[4]:=K5.position/100;
  label13.caption := format('K5: %f',[ki[4]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K6Change(Sender: TObject);
begin
  ki[5]:=K6.position/100;
  label12.caption := format('K6: %f',[ki[5]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K7Change(Sender: TObject);
begin
  ki[6]:=K7.position/100;
  label8.caption := format('K7: %f',[ki[6]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K8Change(Sender: TObject);
begin
  ki[7]:=K8.position/100;
  label9.caption := format('K8: %f',[ki[7]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K9Change(Sender: TObject);
begin
  ki[8]:=K9.position/100;
  label10.caption := format('K9: %f',[ki[8]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.K10Change(Sender: TObject);
begin
  if K10.position=0 then K10.position:=-1;
  ki[9]:=K10.position/100;
  label11.caption := format('K10: %f',[ki[9]]);
  LPC.ki2ai(pki, NOrder, pai); LPC.unitcircle;
end;

procedure Tform1.amplitudeChange(Sender: TObject);
begin
  SIGMA:=amplitude.position/100;
  label31.caption := format('Gain value: %f',[SIGMA]);
end;

procedure Tform1.voicedClick(Sender: TObject);
begin
  bVoiced := true;
  modelChoice(bVoiced);
end;

procedure Tform1.unvoicedClick(Sender: TObject);
begin
  bVoiced := false;
  modelChoice(bVoiced);
end;


procedure Tform1.A0Change(Sender: TObject);
begin
  ai[0]:=A0.position/100;
  label28.caption := format('A0: %f',[ai[0]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A1Change(Sender: TObject);
begin
  ai[1]:=A1.position/100;
  label27.caption := format('A1: %f',[ai[1]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A2Change(Sender: TObject);
begin
  ai[2]:=A2.position/100;
  label26.caption := format('A2: %f',[ai[2]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A3Change(Sender: TObject);
begin
  ai[3]:=A3.position/100;
  label25.caption := format('A3: %f',[ai[3]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A4Change(Sender: TObject);
begin
  ai[4]:=A4.position/100;
  label24.caption := format('A4: %f',[ai[4]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A5Change(Sender: TObject);
begin
  ai[5]:=A5.position/100;
  label23.caption := format('A5: %f',[ai[5]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A6Change(Sender: TObject);
begin
  ai[6]:=A6.position/100;
  label22.caption := format('A6: %f',[ai[6]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A7Change(Sender: TObject);
begin
  ai[7]:=A7.position/100;
  label21.caption := format('A7: %f',[ai[7]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A8Change(Sender: TObject);
begin
  ai[8]:=A8.position/100;
  label20.caption := format('A8: %f',[ai[8]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A9Change(Sender: TObject);
begin
  ai[9]:=A9.position/100;
  label19.caption := format('A9: %f',[ai[9]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.A10Change(Sender: TObject);
begin
  ai[10]:=A10.position/100;
  label18.caption := format('A10: %f',[ai[10]]);
  //Overwrites ki values accordingly to new ai values
  LPC.ai2ki(pai, NOrder, pki);
  LPC.unitcircle;
end;

procedure Tform1.TabSheet5MouseDown(Sender: TObject; Button: TMouseButton; Shift: TShiftState; X, Y: Integer);
begin
  LPC.razFilterMemories;
  //Raz sliders Ki
  K1.position := 0; K2.position := 0; K3.position := 0; K4.position := 0;
  K5.position := 0; K6.position := 0; K7.position := 0; K8.position := 0;
  K9.position := 0; K10.position := -1;
end;

procedure Tform1.PageControl2Change(Sender: TObject);
begin
  if PageControl2.ActivePage.Caption='Ki' then
  begin
    K1.position := round(ki[0]*100);  K2.position := round(ki[1]*100);
    K3.position := round(ki[2]*100);  K4.position := round(ki[3]*100);
    K5.position := round(ki[4]*100);  K6.position := round(ki[5]*100);
    K7.position := round(ki[6]*100);  K8.position := round(ki[7]*100);
    K9.position := round(ki[8]*100); K10.position := round(ki[9]*100);
  end;
  if PageControl2.ActivePage.Caption='Ai' then
  begin
    A0.position := round(ai[0]*100);
    A1.position := round(ai[1]*100);
    A2.position := round(ai[2]*100);
    A3.position := round(ai[3]*100);
    A4.position := round(ai[4]*100);
    A5.position := round(ai[5]*100);
    A6.position := round(ai[6]*100);
    A7.position := round(ai[7]*100);
    A8.position := round(ai[8]*100);
    A9.position := round(ai[9]*100);
    A10.position := round(ai[10]*100);
    A0Change(Self); A1Change(Self); A2Change(Self); A3Change(Self); A4Change(Self); A5Change(Self); A6Change(Self); A7Change(Self); A8Change(Self); A9Change(Self); A10Change(Self);
  end;
end;

procedure TForm1.PlayButtonClick(Sender: TObject);
begin
  if PlayButton.down then
  begin
    RecButton.Down := false;
    recbutton.enabled := false;
    x2.enabled := false;
    LPC.razFilterMemories;
    pagecontrol2.enabled := true;
    voiced.enabled := true;
    unvoiced.enabled := true;
    pitch.enabled := true;
    amplitude.enabled := true;
    AGC.enabled := true;
    PlayStart;
  end
  else
  begin
    PlayStop;
    pagecontrol2.enabled := false;
    voiced.enabled := false;
    unvoiced.enabled := false;
    pitch.enabled := false;
    amplitude.enabled := false;
    AGC.enabled := false;
  end;
end;

procedure Tform1.RecButtonClick(Sender: TObject);
begin
  if RecButton.down then
  begin
    //Test in case the rec_pause button has been pushed
    if bAudioOutStarted then
    begin
      PlayStop;
    end;
    PlayButton.Down := false;
    x2.enabled := true;
    pagecontrol2.enabled := false;
    voiced.enabled := false;
    unvoiced.enabled := false;
    pitch.enabled := false;
    amplitude.enabled := false;
    AGC.enabled := false;
    playbutton.enabled := false;
    RecordStart;
  end
  else
  begin
    RecordStop;
    x2.enabled := false;
  end;
end;

procedure TForm1.FormShow(Sender: TObject);
begin
  PitchVal:=pitch.position/100; label33.caption := format('Pitch value: %f (Hz)',[PitchVal]);
  ki[0]:=K1.position/100;  label17.caption := format('K1: %f',[ki[0]]);
  ki[1]:=K2.position/100;  label16.caption := format('K2: %f',[ki[1]]);
  ki[2]:=K3.position/100;  label15.caption := format('K3: %f',[ki[2]]);
  ki[3]:=K4.position/100;  label14.caption := format('K4: %f',[ki[3]]);
  ki[4]:=K5.position/100;  label13.caption := format('K5: %f',[ki[4]]);
  ki[5]:=K6.position/100;  label12.caption := format('K6: %f',[ki[5]]);
  ki[6]:=K7.position/100;  label8.caption := format('K7: %f',[ki[6]]);
  ki[7]:=K8.position/100;  label9.caption := format('K8: %f',[ki[7]]);
  ki[8]:=K9.position/100;  label10.caption := format('K9: %f',[ki[8]]);
  ki[9]:=K10.position/100; label11.caption := format('K10: %f',[ki[9]]);
  SIGMA:=amplitude.position/100; label31.caption := format('Gain value: %f',[SIGMA]);
  ai[0]:=A0.position/100; label28.caption := format('A0: %f',[ai[0]]);
  ai[1]:=A1.position/100; label27.caption := format('A1: %f',[ai[1]]);
  ai[2]:=A2.position/100; label26.caption := format('A2: %f',[ai[2]]);
  ai[3]:=A3.position/100; label25.caption := format('A3: %f',[ai[3]]);
  ai[4]:=A4.position/100; label24.caption := format('A4: %f',[ai[4]]);
  ai[5]:=A5.position/100; label23.caption := format('A5: %f',[ai[5]]);
  ai[6]:=A6.position/100; label22.caption := format('A6: %f',[ai[6]]);
  ai[7]:=A7.position/100; label21.caption := format('A7: %f',[ai[7]]);
  ai[8]:=A8.position/100; label20.caption := format('A8: %f',[ai[8]]);
  ai[9]:=A9.position/100; label19.caption := format('A9: %f',[ai[9]]);
  ai[10]:=A10.position/100; label18.caption := format('A10: %f',[ai[10]]);
  LPC.drawunitcircle;
end;

procedure TForm1.PageControl2Changing(Sender: TObject; var AllowChange: Boolean);
begin
//
end;

procedure TForm1.SpeedButton1Click(Sender: TObject);
begin
  LPC.razFilterMemories;
end;

procedure TForm1.tubemodelMouseMove(Sender: TObject; Shift: TShiftState; X,
  Y: Integer);
begin
  label2.caption := format('Tube model: %f',[(tubemodel.height-Y)/10]);
end;

function TForm1.AudioOutFillBuffer(Buffer: PChar; var Size: Integer): Boolean;
var pData: ^VSmallInt;
    i, amplitudePosition :integer;
    max: smallInt;
begin
  if form1.PlayButton.down then
    begin
      //Fill current x[] frame
      For i:=0 to FrameSize-1 do
      begin
        if bVoiced then
        begin
          iphase := iphase + 1;
          if iphase>=round(Fe/PitchVal) then iphase:=0;//Phasis incrementation
          if iphase=0 then x[i]:=1 else x[i]:=0;
          //x[i]:=sin(2*pi*PitchVal*iphase/Fe);
        end
        else x[i]:=random*2-1;
      end;

      //Applies LPC model on this frame
      form1.label6.caption:='';
      try
        LPC.Filter_IIR_lattice(pki, NOrder, px, FrameSize, py, pzi, SIGMA);
      except
        on E: EOverflow do form1.label6.caption:=E.message;
        on E: EInvalidOp do form1.label6.caption:=E.message;
      else
        form1.label6.caption:='Unknown exception';
      end;

      //Sends this frame to the sound card, and changes the gain slider value if AGC is enabled
      amplitudePosition := form1.amplitude.position;

      //***************************************
      //ancien emplacement de la fonction SBPlay()

      pData := Pointer(Buffer);
      max:=0;
      for i:=0 to FrameSize-1 do
      begin
        pData^[i] := round(32767*y[i]);
        if form1.AGC.checked then
        begin
          if max<abs(round(32767*y[i])) then max := abs(round(32767*y[i]));
          if abs(round(32767*y[i]))>32780 then dec(amplitudePosition);
        end;
      end;
      if form1.AGC.checked then
      begin
        if max<16384 then inc(amplitudePosition);
      end;

      //***************************************

      if (form1.amplitude.position <> amplitudePosition) then form1.amplitude.position := amplitudePosition;

      //Save up to height frames to get 1024 data for the FFT calculation
      for i:=0 to FrameSize-1 do y2[i + fft_offset] := y[i];
      fft_offset := fft_offset + FrameSize;
      if fft_offset >= 1024 then
      begin
        fft_offset:=0;
        LPC.calculate_plot_time_fft;
      end;
      AudioOutFillBuffer:=True;
   end
   else AudioOutFillBuffer := false;
   //End of frame
end;

function TForm1.AudioInBufferFilled(Buffer: PChar; var Size: Integer): Boolean;
var i :integer;
    sigmaAutocorr: double;
    pData: ^VSmallInt;
begin
  //Sends this frame to the sound card
  if form1.RecButton.down then
    begin
    pData := Pointer(Buffer);

    for i:=0 to FrameSize-1 do
    begin
      if not form1.x2.checked then y[i] := pData^[i]/32767
      else y[i] := pData^[i]/16384;
    end;

    //Save up to height frames to get 1024 data for the FFT calculation
    for i:=0 to FrameSize-1 do y2[i + fft_offset] := y[i];
    fft_offset := fft_offset + FrameSize;
    if fft_offset >= 1024 then
    begin
      fft_offset:=0;
      LPC.calculate_plot_time_fft;
    end;

    inc(nCalculateSchur);
    if nCalculateSchur >= 0 then
    begin
      //Calculate the autocorrelation sequence and the ki
      LPC.hamming(py, FrameSize);
      LPC.Autocorr(py, FrameSize, pAtcr, NOrder);
      sigmaAutocorr := LPC.Schur(pAtcr, NOrder, pki);
      LPC.ki2ai(pki, NOrder, pai);
      LPC.unitcircle;
      nCalculateSchur:=0;
      LPC.updateCoefLabels;
      SIGMA:=sigmaAutocorr;
    end;
    AudioInBufferFilled:=True;
  end
  else AudioInBufferFilled:=False;
  //End of frame
end;

procedure TForm1.PlayStop;
begin
  AudioOut.StopGraceFully;
end;

procedure TForm1.PlayStart;
begin
  if (not AudioOut.Start(AudioOut)) then
  begin
//    ShowMessage('Audio Out failed because: ' + ^M + AudioOut.ErrorMessage);
    AudioOut.StopAtOnce;
    AudioOut.Start(AudioOut)
  end;
end;

procedure TForm1.RecordStop;
begin
  AudioIn.StopAtOnce;
end;

procedure TForm1.RecordStart;
begin
  if (not AudioIn.Start(AudioIn)) Then
    ShowMessage(AudioIn.ErrorMessage);
end;

procedure TForm1.AudioOutStop(Sender: TObject);
begin
  bAudioOutStarted:=False;
  recbutton.enabled := true;
end;

procedure TForm1.AudioOutStart(Sender: TObject);
begin
  bAudioOutStarted:=True;
end;

procedure TForm1.AudioInStart(Sender: TObject);
begin
  bAudioInStarted:=True;
end;

procedure TForm1.AudioInStop(Sender: TObject);
begin
  bAudioInStarted:=False;
  playbutton.enabled := true;
end;

procedure TForm1.Savetofile1Click(Sender: TObject);
var F: file of double;
    i:integer;
begin
  //save param from memory to file
  assignfile(F,'lpclearn.dat');
  rewrite(F);
  for i:=0 to NOrder do write(F,ki[i]);
  write(F, SIGMA);
  closefile(F);
end;

procedure TForm1.Savetomemory1Click(Sender: TObject);
var i:integer;
begin
  for i:=0 to NOrder do kimem[i] := ki[i];
  sigmaMem:= SIGMA;
end;

procedure TForm1.Loadfrommemory1Click(Sender: TObject);
var i:integer;
begin
  for i:=0 to NOrder do ki[i] := kimem[i];
  SIGMA:=sigmaMem;
  amplitude.Position := round(SIGMA*100);
  LPC.ki2ai(pki, NOrder, pai);
  PageControl2Change(Self);
end;

procedure TForm1.Loadfromfile1Click(Sender: TObject);
var F: file of double;
    i:integer;
begin
  //save param from memory to file
  assignfile(F,'lpclearn.dat');
  reset(F);
  for i:=0 to NOrder do read(F, ki[i]);
  write(F, SIGMA);
  closefile(F);

  amplitude.Position := round(SIGMA*100);
  LPC.ki2ai(pki, NOrder, pai);
  PageControl2Change(Self);
end;

procedure TForm1.MSClick(Sender: TObject);
begin
  SaveMenu.Popup(MS.Left+10, MS.Top+30);
end;

procedure TForm1.MRClick(Sender: TObject);
begin
  LoadMenu.popup(MR.Left+10, MR.Top+30);
end;

end.
