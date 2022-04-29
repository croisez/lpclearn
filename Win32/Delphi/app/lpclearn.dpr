program lpclearn;

uses
  Forms,
  lpclearn1 in 'lpclearn1.pas' {Form1};

{$R *.RES}

begin
  Application.Initialize;
  Application.Title := 'Demonstration of the LPC synthesis filter';
  Application.CreateForm(TForm1, Form1);
  Application.Run;
end.
