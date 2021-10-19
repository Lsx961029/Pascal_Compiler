var x: real;
var b: boolean;
var count: integer;

begin
    x := 1.0;

    while x < 32.01 do
    begin
        writeln(x);
        x := x+1;
    end;

    b := true;

    while b do
    begin
        for count:= 5 to 11 do
        begin
            writeln(b);
            if b then
            begin
                b:=false;
            end;
        end;
    end;
    
end.