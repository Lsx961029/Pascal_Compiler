var
    num: array [1..2] of integer;
var
    i: integer;
begin

    for i := 1 to 2 do
    begin
        
        num[ i ] := i;
    end;
    writeln(num[3]);
end.