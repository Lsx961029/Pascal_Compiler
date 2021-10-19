var x: integer;
var b: boolean;

begin
    b := true;
    x := 1;

    repeat writeln(x); x := x + 1;
    until x > 10;


    repeat writeln(x); x := x + 1;
    until 1;

    repeat writeln(x); x := x + 1;
    until true;

    repeat writeln(x); x := x + 1;
    until b;

    
    

    writeln(x);
end.