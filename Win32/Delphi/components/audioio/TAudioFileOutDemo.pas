unit TAudioFileOutDemo;

interface

uses
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  Buttons, ExtCtrls, FileCtrl, StdCtrls, UAFDefs, AudioIO,
  ComCtrls;

type
  TForm1 = class(TForm)
    Panel1: TPanel;
    PlaySpeedButton: TSpeedButton;
    AudioOut1: TAudioOut;
    DriveComboBox1: TDriveComboBox;
    DirectoryListBox1: TDirectoryListBox;
    FileListBox1: TFileListBox;
    FilterComboBox1: TFilterComboBox;
    Edit1: TEdit;
    Label1: TLabel;
    TypeLabel: TLabel;
    FormatLabel: TLabel;
    Label2: TLabel;
    Label3: TLabel;
    Label4: TLabel;
    PropertyLabel: TLabel;
    Bevel2: TBevel;
    Label5: TLabel;
    SizeLabel: TLabel;
    TimeLabel: TLabel;
    ProgressBar1: TProgressBar;
    Timer1: TTimer;

    procedure PlaySpeedButtonClick(Sender: TObject);
    function AudioOut1FillBuffer(B: PChar; Var N: Integer): Boolean;
    procedure FileListBox1Click(Sender: TObject);
    procedure AudioOut1Stop(Sender: TObject);
    Procedure UpdateAudioInfo(FileName : String);
    procedure AudioOut1Start(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
  private
    { Private declarations }
    WasStereo : Boolean;
    ReadSize  : Integer;
    UAF       : UAF_File;
    Buffer    : ^Integer;
    lPos      : Integer;

  public
    { Public declarations }
    Function SetupStart(FileName : String) :Boolean;
  end;

var
  Form1: TForm1;

implementation

{$R *.DFM}

procedure TForm1.PlaySpeedButtonClick(Sender: TObject);
begin
  If (PlaySpeedButton.Down) Then
    Begin
      If (Not SetupStart(Edit1.Text)) Then
        Begin
          ShowMessage('Failed to setup start, because:' + ^m + AudioOut1.ErrorMessage);
          Exit;
        End;

      If (Not AudioOut1.Start(AudioOut1)) Then
          ShowMessage('Failed to start audio, because:' + ^m + AudioOut1.ErrorMessage);

    End
  Else
    Begin
      AudioOut1.StopAtOnce;
    End;

  PlaySpeedButton.Down := AudioOut1.Active;
end;

function TForm1.AudioOut1FillBuffer(B: PChar; Var N: Integer): Boolean;
Var
  SP, DP, SPL, SPR, DPR, DPL : ^SmallInt;
  vl, vr : Smallint;
  br, i : Integer;
  x : Real;

begin

  { This will happen on an error }
  If (N <= 0) or (Not Active) Then
    Begin
       Result := FALSE;
       Exit;
    End;

  { Read in a buffer }
  FillMemory(Buffer,ReadSize, 0);
  br := UAF_Read(UAF, Buffer, ReadSize div UAF.FrameSize, lPos);
  If (Br = 0) Then
    Begin
      Result := FALSE;
      Exit;
    End;

  lPos := lPos + br;

  { The input for UAF files is ALWAYS PCM, Signed Small Integer }

    { If the file was mono, fade left to right }
     If (Not WasStereo) Then
        Begin
          SP := Pointer(Buffer);
          DP := Pointer(B);

         { NOTE! we change the size of N, BE careful, only do so if you really
           want a less number of points to be played }
          N := br*2*UAF.FrameSize;
          For i := 0 to (N div (2*UAF.FrameSize))-1 Do
          Begin
           { Now compute the fade rate from one channel to another. Not interesting }
            x := ((AudioOut1.FilledBuffers*ReadSize div UAF.FrameSize) + i) / UAF.Frames;
            DP^ := Round((1-x)*SP^);
            Inc(DP);
            DP^ := Round(x*SP^);
            Inc(DP);
            Inc(SP);
          End;
        End
     Else
       { File is stereo, just mix left into right and visa versa }
       Begin
         SPL := Pointer(Buffer);
         SPR := SPL;
         Inc(SPR);
         DPL := Pointer(B);
         DPR := DPL;
         Inc(DPR);

         { NOTE! we change the size of N, BE careful, only do so if you really
           want a less number of points to be played }
         N := br*UAF.FrameSize;
         For i := 0 to (N div 4) - 1 Do
           Begin
            { Now compute the fade rate from one channel to another. Not interesting }
             x := ((AudioOut1.FilledBuffers*ReadSize div UAF.FrameSize) + i) / UAF.Frames;
             vl := SPL^;
             vr := SPR^;
             DPL^ := Round((1-x)*vr + x*vl);
             DPR^ := Round(x*vr + (1-x)*vl);
             Inc(SPR,2); Inc(SPL,2);
             Inc(DPR,2); Inc(DPL,2);
             End;
       End;

  Result := TRUE;
end;

procedure TForm1.FileListBox1Click(Sender: TObject);
begin
  PlaySpeedButton.Enabled := TRUE;
  UpdateAudioInfo(FileListBox1.Items[FileListBox1.ItemIndex]);
end;

procedure TForm1.AudioOut1Stop(Sender: TObject);
begin
   PlaySpeedButton.Down := FALSE;
//   Timer1.Enabled := FALSE;
   If (Buffer <> Nil) Then FreeMem(Buffer, ReadSize);
   Buffer := Nil;
   UAF_Close(UAF);
end;

Function TForm1.SetupStart(FileName : String) :Boolean;
begin
  If (Not UAF_Open(UAF, FileName, 'r', UAF_TYPE_UNKNOWN)) Then
     Begin
        AudioOut1.ErrorMessage := UAF_ErrorMessage;
        Result := FALSE;
        Exit;
     End;

  { Setup all the sampling parameters }
  lPos := 0;
  AudioOut1.FrameRate := Round(UAF.FrameRate);
  AudioOut1.Stereo := (UAF.Channels <> 1);
  AudioOut1.Quantization := 16;

  WasStereo := AudioOut1.Stereo;
  If (WasStereo) Then
     ReadSize := AudioOut1.BufferSize
  Else
     ReadSize := AudioOut1.BufferSize Div 2;

  GetMem(Buffer, ReadSize);
  If (Buffer = Nil) Then
    Begin
       AudioOut1.ErrorMessage := 'Could Not alloc buffer';
       Result := FALSE;
       Exit;
    End;
  AudioOut1.Stereo := TRUE;
  Result := TRUE;
end;

{--------UpdateAudioInfo------------------John Mertus---May 97---}

   Procedure  TForm1.UpdateAudioInfo(FileName : String);

{  This procedure does all the hard work of opening the file and }
{  fillings in the information about the file into the form      }
{                                                                }
{****************************************************************}

{ UAF variables }
Var
   UAFIn            : UAF_File;
   Fin              : File of Byte;
   S                : String;
   xFS              : LongInt;

Begin
 { Find the file size }
  AssignFile(fin,FileName);
  Reset(fin);
  xFS := FileSize(fin);
  CloseFile(fin);

  { Open the file }
  If (Not UAF_Open(UAFIn, FileName, 'r', UAF_TYPE_UNKNOWN)) Then
    Begin
      TypeLabel.Caption := UAF_ErrorMessage;
      FormatLabel.Caption := '';
      PropertyLabel.Caption := '';
      Exit;
    End;

  TypeLabel.Caption := String(UAF_Identity(UAFIn));


  { Finish up with the rate and bits }
   FormatLabel.Caption := UAF_Description(UAFIn);
   S := Format('%0.0n Hz, %d Bit ',[UAFIn.FrameRate, UAFIn.Quantization]);
   If (UAFIn.Channels = 1) Then
     S := S + 'Mono'
   Else if (UAFIn.Channels = 2) Then
     S := S + 'Stereo'
   Else
     S := S + Format('%d Channels',[UAFIn.Channels]);

   PropertyLabel.Caption := S;

   SizeLabel.Caption := Format('%0.3n Seconds (%0.0n Bytes) ',
                  [UAFIn.Frames/UAFIn.FrameRate, xFS*1.0]);
   UAF_Close(UAFIn);
End;

procedure TForm1.AudioOut1Start(Sender: TObject);
begin
   Timer1.Enabled := TRUE;
end;

procedure TForm1.Timer1Timer(Sender: TObject);
Var
 x : Real;
begin
  x := AudioOut1.ElapsedTime;
  TimeLabel.Caption := Format('%0.3n Seconds', [x]);
  ProgressBar1.Position := Round(100*x*UAF.FrameRate/UAF.Frames);
end;

end.
