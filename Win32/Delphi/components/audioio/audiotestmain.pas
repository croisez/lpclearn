unit audiotestmain;

interface

uses
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs,
  StdCtrls, AudioIO, ExtCtrls, Buttons, ComCtrls, MMSYSTEM;

type
  TForm1 = class(TForm)
    Label1: TLabel;
    wave: TPaintBox;
    Timer1: TTimer;
    AudioIn1: TAudioIn;
    procedure Timer1Timer(Sender: TObject);
    procedure StopButtonClick(Sender: TObject);
    function AudioIn1BufferFilled(Buffer: PChar;
      var Size: Integer): Boolean;
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure FormCreate(Sender: TObject);
  private
    { Private declarations }
  public
  end;

var
  Form1: TForm1;
  v:integer;
implementation

{$R *.DFM}

function TForm1.AudioIn1BufferFilled(Buffer: PChar; var Size: Integer): Boolean;
Var
  SP    : ^SmallInt;
  i     : Integer;
  xs    :integer;
  ys    :integer;
begin
  SP := Pointer(Buffer);
  xs:=wave.width ;
  ys:=wave.height;
  { erase background }
  wave.canvas.brush.color:=clWindow;
  wave.canvas.CopyMode:=cmSrcCopy;
  //wave.canvas.FillRect(Rect(0,0,xs,ys));
  wave.canvas.rectangle(0,0,xs,ys);
  wave.canvas.brush.color:=clGreen;
  For i := 0 to xs-1 Do
  Begin
    v := SP^; Inc(SP);
    { plot }
    wave.canvas.MoveTo(i,ys div 2);
    wave.canvas.LineTo(i,ys div 2-round(v/32768*ys));
  End;
  Result := TRUE;
end;

procedure TForm1.Timer1Timer(Sender: TObject);
begin
Label1.Caption := intToStr(v);
Application.ProcessMessages;
end;

procedure TForm1.StopButtonClick(Sender: TObject);
begin
  AudioIn1.StopAtOnce;
end;

procedure TForm1.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  AudioIn1.StopAtOnce;
end;

procedure TForm1.FormCreate(Sender: TObject);
begin
   If (Not AudioIn1.Start(AudioIn1)) Then ShowMessage(AudioIn1.ErrorMessage)
end;

end.
