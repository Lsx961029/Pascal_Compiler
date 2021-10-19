var count: integer;
var a: real;
var c: char;

begin
    a := 1.01;
    c :=' ';
    for count:= 5 to 11 do
    begin
        a := a*count;
        writeln(count);
        writeln(a);
        writeln(c);
    end;
end.