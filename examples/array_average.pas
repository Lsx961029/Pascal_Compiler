var
    num: array [5..50] of integer;
var
    i, sum: integer;
var
    average : real;

begin
    sum := 0;

    for i := 5 to 50 do
    begin
        writeln(i);
        num[ i ] := i;
        sum := sum + num[ i ];
    end;
    writeln('i');
    writeln(num[51]);
    average := sum / 10.0;
    writeln(sum);
    writeln(average);
end.