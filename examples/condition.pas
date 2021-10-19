var a,b,c: boolean;
var i: integer;
begin
    a:= true;
    b:= false;

    c:= a or b;
    writeln(c);

    c:= not a or b;
    writeln(c);

    c:= not a xor b;
    writeln(c);

    c:= not(a and (false xor b) and c);
    writeln(c);

    c:= not (a and b);
    writeln(c);

    writeln(a and 0);

    writeln(1 or false);

    writeln(true or false);

    b := not true;
    if not b then
    begin
    writeln(not b);
        else
    writeln(b);
    end;
    
end.
