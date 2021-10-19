var ints, ints2: array[0..10] of integer;
var reals: array[10..50] of real;
var chars: array[5..9] of char;
var bools: array[0..3] of boolean;
var array2: array['a'..'e'] of real;
var i: integer;
var result: real;

begin
    for i:= 0 to 10 do
    begin
        ints[i]:=i*2;
    end;

    for i:= 0 to 10 do
    begin
        writeln(ints[i]);
    end;


    reals[10] := 0.0;
    reals[11] := 1.0;
    for i:= 12 to 30 do
    begin
        reals[i] := reals[i-1] + reals[i-2];
    end;

    for i:= 10 to 30 do
    begin
        writeln(reals[i]);
    end;

    chars[1]:= 'a';
    writeln(chars[1]);

    bools[0]:= true;
    bools[1]:= false;
    bools[3]:= not(bools[0] and bools[1]);
    writeln(bools[3]);

    array2['e'] := 3.14;
    writeln(array2['e']);

    writeln(bools[ints[4] div ints[3]]);

end.

