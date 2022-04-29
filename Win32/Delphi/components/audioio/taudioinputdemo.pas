unit TAudioInputDemo;

interface

uses
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  StdCtrls, AudioIO, ExtCtrls, Buttons, ComCtrls, MMSYSTEM;

type
  TForm1 = class(TForm)
    StartButton: TButton;
    Timer1: TTimer;
    StopButton: TButton;
    RunStatusLabel: TLabel;
    BufferStatusLabel: TLabel;
    TimeStatusLabel: TLabel;
    Panel1: TPanel;
    RecordSpeedButton: TSpeedButton;
    ProgressBar1: TProgressBar;
    MaxLabel: TLabel;
    AudioIn1: TAudioIn;
    procedure StartButtonClick(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
    procedure AudioIn1Stop(Sender: TObject);
    procedure UpdateStatus;
    procedure StopButtonClick(Sender: TObject);
    procedure RecordSpeedButtonClick(Sender: TObject);
    function AudioIn1BufferFilled(Buffer: PChar;
      var Size: Integer): Boolean;
  private
    { Private declarations }
  public
    { Public declarations }
    Min, Max : Integer;
    TempMax  : Integer;
  end;

var
  Form1: TForm1;

implementation

{$R *.DFM}

procedure TForm1.StartButtonClick(Sender: TObject);
begin
   If (Not AudioIn1.Start(AudioIn1)) Then ShowMessage(AudioIn1.ErrorMessage)
   Else
     Begin
        Min := 0;
        Max := 0;
        RecordSpeedButton.Down := TRUE;
     End;
end;

function TForm1.AudioIn1BufferFilled(Buffer: PChar; var Size: Integer): Boolean;
Var
  SP    : ^SmallInt;
  i, N,  v  : Integer;
  xMin, xMax : Integer;

begin
  N := Size Div 2;
  SP := Pointer(Buffer);
  xMin := SP^;
  xMax := xMin;


  For i := 0 to N-1 Do
     Begin
       v := SP^; Inc(SP);
       If (xMin > v) Then xMin := v;
       If (xMax < v) Then xMax := v;
     End;

  If (Min > xMin) Then Min := xMin;
  If (Max < xMax) Then Max := xMax;

  TempMax := xMax;
  If (Abs(xMin) > xMax) Then TempMax := Abs(xMin);
  Result := TRUE;
end;

Procedure TForm1.UpdateStatus;
begin
  With AudioIn1 Do
   If (AudioIn1.Active) Then
     Begin
       RunStatusLabel.Caption := 'Started';
       BufferStatusLabel.Caption := Format('Queued: %3d;  Processed: %3d',[QueuedBuffers, ProcessedBuffers]);
       TimeStatusLabel.Caption := Format('Seconds %.3n',[ElapsedTime]);
     End
   Else
     Begin
       RunStatusLabel.Caption := 'Stopped';
       BufferStatusLabel.Caption := '';
       TimeStatusLabel.Caption := '';
     End;

   { Update the progress bar }
   If (AudioIn1.Active) Then
     Begin
       ProgressBar1.Position := Round(100*TempMax/36768.0);
       If (Abs(Min) > Max) Then Max := Abs(Min);
       MaxLabel.Caption := Format('Max %5d;  Peak %5d',[Max,TempMax]);
     End
   Else
     Begin
       ProgressBar1.Position := 0;
       MaxLabel.Caption := '';
     End;

End;

procedure TForm1.Timer1Timer(Sender: TObject);
begin
  UpdateStatus;
end;

procedure TForm1.AudioIn1Stop(Sender: TObject);
begin
   RecordSpeedButton.Down := FALSE;
end;


procedure TForm1.StopButtonClick(Sender: TObject);
begin
  AudioIn1.StopAtOnce;
end;

procedure TForm1.RecordSpeedButtonClick(Sender: TObject);
begin
  If (RecordSpeedButton.Down) Then
     StartButtonClick(Sender)
  Else
     AudioIn1.StopAtOnce;
end;

end.
