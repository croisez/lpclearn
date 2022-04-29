unit TAudioSineDemo;

{
   This just demostrates some of the functions of the TSoundOut component
      1) How to Fill Buffers.
      2) How to Start, Stop at once, Stop gracefully.
      3) How to pause and resume playout.
      4) Using OnStart and OnStop.
}

interface

uses
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  Menus, StdCtrls, Buttons,
  ExtCtrls, ComCtrls, AudioIO;

type
  TForm1 = class(TForm)
    StartButton: TButton;
    RunStatusLabel: TLabel;
    StopButton: TButton;
    BufferStatusLabel: TLabel;
    TimeStatusLabel: TLabel;
    Timer1: TTimer;
    BufferEdit: TEdit;
    BufferLabel: TLabel;
    FreqLabel: TLabel;
    TrackBar1: TTrackBar;
    PauseButton: TButton;
    AudioOut1: TAudioOut;
    TrackBar2: TTrackBar;
    Label1: TLabel;
    ScrollBar1: TScrollBar;
    ScrollBar2: TScrollBar;
    procedure StartButtonClick(Sender: TObject);
    function AudioOut1FillBuffer(Buffer: PChar; Var N: Integer): Boolean;
    procedure StopButtonClick(Sender: TObject);
    procedure AudioOut1Stop(Sender: TObject);
    procedure SoundOutButtonClick(Sender: TObject);
    Procedure UpdateStatus;
    procedure Timer1Timer(Sender: TObject);
    procedure BufferEditExit(Sender: TObject);
    procedure AudioOut1Start(Sender: TObject);
    procedure TrackBar1Change(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure PauseButtonClick(Sender: TObject);
    procedure TrackBar2Change(Sender: TObject);
  private
    { Private declarations }
    TotalBuffers : Integer;
    CFreq        : Integer;
    MFreq        : Integer;

  public
    { Public declarations }
  end;

var
  Form1: TForm1;

implementation

{$R *.DFM}

procedure TForm1.StartButtonClick(Sender: TObject);
Var
  iErr : Integer;
begin
  Val(BufferEdit.Text, TotalBuffers, iErr);

  If (Not AudioOut1.Start(AudioOut1)) Then ShowMessage('Audio Out failed because: ' + ^M + AudioOut1.ErrorMessage);
end;

function TForm1.AudioOut1FillBuffer(Buffer: PChar; Var N: Integer): Boolean;
{
  Whenever the component needs another buffer, this routine is called,
  N is the number of BYTES required, B the the address of the buffer.
}
Var
  NW, i, ts : Integer;
  P : ^SmallInt;

begin
  { See if we want to quit.  Process TotalBuffers except if TotalBuffer
    is <= 0, then process forever. }
   If (AudioOut1.QueuedBuffers >=  TotalBuffers) and (TotalBuffers > 0) Then
     Begin
       { Stop processing by just returning FALSE }
       Result := FALSE;
       Exit;
     End;;

   { First step, cast the buffer as the proper data size, if this output
     was 8 bits, then the cast would be to ^Byte.  N now represents the
     total number of 16 bit words to process. }
   P := Pointer(Buffer);
   NW := N div 2;

   { Now create a sine wave, because the buffer may not align with the end
     of a full sine cycle, we must compute it using the total number of
     points processed.  FilledBuffers give the total number of buffer WE
     have filled, so we know the number of point WE processed }

   ts := NW*AudioOut1.FilledBuffers;
   { Note: Freq is set from the TrackBar }
   For i := 0 to NW-1 Do
     Begin
      P^ := Round(8192/2*(
            Sin((ts+i)/AudioOut1.FrameRate*3.1415926*2*CFreq) +
            Sin((ts+i)/AudioOut1.FrameRate*3.1415926*2*MFreq)
            ));
      Inc(P);
     End;

   { True will continue Processing }
   Result := True;
end;

procedure TForm1.StopButtonClick(Sender: TObject);
begin
  AudioOut1.StopGraceFully;
end;

procedure TForm1.AudioOut1Stop(Sender: TObject);
begin
  SoundOutButton.Down := FALSE;
end;

procedure TForm1.SoundOutButtonClick(Sender: TObject);
begin
   If (Not SoundOutButton.Down) Then
     AudioOut1.StopAtOnce
   Else
     StartButtonClick(Sender);
end;

Procedure TForm1.UpdateStatus;
begin
  With AudioOut1 Do
   If (AudioOut1.Active) Then
     Begin
       If (Not AudioOut1.Paused) Then
          RunStatusLabel.Caption := 'Playing Out'
       Else
          RunStatusLabel.Caption := 'Started, Paused';

       BufferStatusLabel.Caption := Format('Queued: %d;  Processed: %d',[QueuedBuffers, ProcessedBuffers]);
       TimeStatusLabel.Caption := Format('Seconds %.3n',[ElapsedTime]);
     End
   Else
     Begin
       If (AudioOut1.Paused) Then
         RunStatusLabel.Caption := 'Not Started, Paused'
       Else
         RunStatusLabel.Caption := 'Not Started';
       BufferStatusLabel.Caption := '';
       TimeStatusLabel.Caption := '';
     End;

   If (AudioOut1.Paused) Then
     PauseButton.Caption := '&Resume'
   Else
     PauseButton.Caption := '&Pause';

End;

procedure TForm1.Timer1Timer(Sender: TObject);
begin
  UpdateStatus;
end;

procedure TForm1.BufferEditExit(Sender: TObject);
Var
  iErr : Integer;

begin
  Val(BufferEdit.Text, TotalBuffers, iErr);
  If (iErr <> 0) Then  ShowMessage('Buffer value must be an integer');
end;

procedure TForm1.AudioOut1Start(Sender: TObject);
begin
  SoundOutButton.Down := TRUE;
end;

procedure TForm1.TrackBar1Change(Sender: TObject);
begin
   CFreq := 100 + 10*TrackBar1.Position;
   FreqLabel.Caption := Format('Carrier Frequency %d',[CFreq]);
end;

procedure TForm1.FormCreate(Sender: TObject);
begin
 TrackBar1Change(Sender);
 TrackBar2Change(Sender);
end;

procedure TForm1.PauseButtonClick(Sender: TObject);
begin
   AudioOut1.Paused := Not AudioOut1.Paused;
end;

procedure TForm1.TrackBar2Change(Sender: TObject);
begin
   MFreq := 100 + 10*TrackBar2.Position;
   Label1.Caption := Format('Modulator Frequency %d',[MFreq]);
end;

end.

