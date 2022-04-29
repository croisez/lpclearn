{**********************************************************}
{ LPC                                                      }
{ Copyright 2000 FAculté Polytechnique de Mons-L.M. Croisez}
{                                                          }
{ This component is FreeWare and can freely be distributed }
{ as long as the source of this component contains the     }
{ the name of the original creator of this component       }
{                                                          }
{ Louis Marie Croisez                                      }
{ croisez@tcts.fpms.ac.be                                  }
{**********************************************************}

unit lpc;

interface
uses
  Classes;
type
  complex      = record Re : double; Im : double; end;
  VComplex     = Array [0..4000] of complex;
  VDouble0     = Array [0..8000] of double;
  VDouble1     = Array [0..1024] of double;//big frame
  VDouble2     = Array [0..128] of double;//little frame
  VDouble3     = Array [0..15] of double; //filter coef
  VComplexPtr  = ^VComplex;
  VDouble0Ptr  = ^VDouble0;
  VDouble1Ptr  = ^VDouble1;
  VDouble2Ptr  = ^VDouble2;
  VDouble3Ptr  = ^VDouble3;

  TLPC = class(TComponent)
    procedure razFilterMemories;
    procedure PlayMainLoop;
    procedure RecMainLoop;
    procedure SBOutInit;
    procedure SBOutClose;
    procedure SBInInit;
    procedure SBInClose;
    function  SBPlay : integer;
    function  SBRecord : integer;
    procedure calculate_plot_freqenv;//math & graph part of fft envelope.
    procedure roots1(A : VDouble0Ptr;Order : integer;Roots : VComplexPtr);
    procedure ai2ki(ai : VDouble0Ptr;Order : integer; ki : VDouble0Ptr);
    procedure ki2ai(ki : VDouble0Ptr;Order : integer; ai : VDouble0Ptr);
    procedure unitcircle;     //math part of the unitcircle process.
    procedure drawunitcircle; //graph part of the unitcircle process.
    procedure drawRootsInTheUnitCircle;
    procedure calculate_plot_time_fft;//math & graph part of time & fft of the output of the synthesis filter.
    procedure Filter_IIR_lattice(ki : VDouble0Ptr; DenOrder : integer; x : VDouble0Ptr; NPoint : integer; y : VDouble0Ptr; zi : VDouble0Ptr; SIGMA : double);
    procedure hamming(Ham : VDouble0Ptr;N : Integer );
    Procedure Autocorr(RealSignal : VDouble0Ptr; NPoints : integer; Atcr : VDouble0Ptr; MaxOrder : Integer);
    function  Schur(Autocorr :VDouble0Ptr;Order : integer; Parcor :VDouble0Ptr): Double;
  private
    { Private declarations }
  public
    { Public declarations }
  end;

implementation
uses lpclearn1;

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
	GetMem(aint,(Order+1)*SizeOf(double));
	for i:=0 to Order-1 do ki^[i]:=ai^[i+1];
	for m:=Order downto 1 do
		begin
		for i:= 1 to m-1 do
			aint^[i]:=(ki^[i-1]-ki^[m-1]*ki^[m-i-1])/(1-ki^[m-1]*ki^[m-1]);
		for i:=1 to m-1 do ki^[i-1]:=aint^[i];
		end;
	FreeMem(aint,(Order+1)*SizeOf(double));
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

procedure TLPC.SBOutInit;
var res: integer;
begin
  res := form1.SBPIPE.OpenWaveOut(3*FRAMESIZE, FRAMESIZE, 8000, 3.0);
  if res <> 0 then showmessage('Error in SBPIPE.OpenWaveOut ='+ inttostr(res));
end;

procedure TLPC.SBInInit;
var res: integer;
begin
  res := form1.SBPIPE.OpenWaveIn(3*FRAMESIZE, FRAMESIZE, 8000, 3.0);
  if res <> 0 then showmessage('Error in SBPIPE.OpenWaveIn ='+ inttostr(res));
end;

procedure TLPC.SBOutClose;
begin
  form1.SBPIPE.CloseWaveOut;
end;

procedure TLPC.SBInClose;
begin
  form1.SBPIPE.CloseWaveIn;
end;

function TLPC.SBPlay : integer;
var data: array[0..1023] of smallInt;
    i: integer;
    pData: pSmallInt;
    max: smallInt;
Begin
 with form1 do
 begin
  max:=0;
  pData := @data;
  for i:=0 to FRAMESIZE-1 do
  begin
    data[i] := round(32767*y[i]);
    if AGC.checked then
    begin
      if max<abs(round(32767*y[i])) then max := abs(round(32767*y[i]));
      if abs(round(32767*y[i]))>32780 then amplitude.Position := amplitude.Position - 1;
    end;
  end;
  if AGC.checked then
  begin
    if max<16384 then amplitude.Position := amplitude.Position + 1;
  end;
  if PlayButton.down then SBPlay := SBPIPE.FillBufferOut(pData, FRAMESIZE)
  else SBPlay := -1;
 end;
end;

function TLPC.SBRecord : integer;
var data: array[0..1023] of smallInt;
    i: integer;
    pData: pSmallInt;
Begin
 with form1 do
 begin
  pData := @data;
  if RecButton.down then SBRecord := SBPIPE.FillBufferIn(pData, FRAMESIZE)
  else SBRecord := -1;

  for i:=0 to FRAMESIZE-1 do
  begin
    y[i] := data[i]/32767;
  end;
 end;
end;

Procedure TLPC.PlayMainLoop;
var i :integer;
begin
      form1.MMTimerPlay.enabled:=false;
      form1.MMTimerPlay.Interval := 29;

      //Fill current x[] frame
      For i:=0 to FRAMESIZE-1 do
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
      LPC.Filter_IIR_lattice(pki, NOrder, px, FRAMESIZE, py, pzi, SIGMA);
      //Sends this frame to the sound card until it is accepted
      LPC.SBPlay;

      //Save up to height frames to get 1024 data for the FFT calculation
      for i:=0 to FRAMESIZE-1 do y2[i + fft_offset] := y[i];
      fft_offset := fft_offset + FRAMESIZE;
      if fft_offset >= 1024 then
      begin
        fft_offset:=0;
        LPC.calculate_plot_time_fft;
      end;

      //End of a frame
      form1.MMTimerPlay.enabled:=true;
end;

Procedure TLPC.RecMainLoop;
var i :integer;
begin
      form1.MMTimerRecord.enabled:=false;
      form1.MMTimerRecord.Interval := 40;

      //Sends this frame to the sound card
      LPC.SBRecord;

      //Save up to height frames to get 1024 data for the FFT calculation
      for i:=0 to FRAMESIZE-1 do y2[i + fft_offset] := y[i];
      fft_offset := fft_offset + FRAMESIZE;
      if fft_offset >= 1024 then
      begin
        fft_offset:=0;
        LPC.calculate_plot_time_fft;
      end;

      inc(nCalculateSchur);
      if nCalculateSchur >= 10 then
      begin
        //Calculate the autocorrelation sequence and the ki
//        LPC.hamming(py, FRAMESIZE);
        LPC.Autocorr(py, FRAMESIZE, pAtcr, NOrder);
        LPC.Schur(pAtcr, NOrder, pki);
        LPC.ki2ai(pki, NOrder, pai);
        LPC.unitcircle;
        nCalculateSchur:=0;
      end;

      //End of a frame
      form1.MMTimerRecord.enabled:=true;
end;

procedure TLPC.hamming(Ham : VDouble0Ptr;N : Integer );
{	Stores an N points Hamming Window in Ham
{	not MATLAB compatible (MATLAB=error!).
{	REF = Harris, Proc. IEEE ASSP 78, vol 66, p.62.
}
var I: Integer;
Begin
  For I:=0 To N-1 Do HAM^[I]:= 0.54-0.46*COS(2*pi*I/N);
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
	For I:=1 TO Order Do Begin V^[I-1]:=Autocorr^[I]; U^[I-1]:=Autocorr^[I-1] End;
	For I:=1 To Order Do
		Begin
		K:=Order-I;
		PARCOR^[I-1]:=-V^[0]/U^[0];
		U^[0]:=U^[0]+PARCOR^[I-1]*V^[0];
		For J:=1 To K Do
			Begin
			V^[J-1]:=PARCOR^[I-1]*U^[J]+V^[J];
			U^[J]:=U^[J]+PARCOR^[I-1]*V^[J]
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

end.
