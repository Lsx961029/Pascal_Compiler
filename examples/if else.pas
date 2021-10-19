var x, y: real;
var z, int:integer;
var b: boolean;
var real1, real2: real;


begin
    x := 100.0;
    y := 100.1;
    z := 100;
    b := true;

    if b then
    begin
        writeln(b);
    end;

    if x = z then
    begin
        if x< 99.9 then
        begin
            writeln(x);
        else
            writeln(z);
        end;
        writeln(y);
    else
        writeln(y);
    end;
    writeln(x);

    b := false;
    if b=true then
    begin
        writeln(b);
        else
        writeln('a');
    end;

    int:= 4;
    real1:= 4.0;
    real2:= 99.9;
    if int <= real1 then
    begin
        if int <> real1 then 
        begin 
            writeln(int);
        else
            writeln(real1);
        end;
    else
        writeln(real2);
    end;

end.