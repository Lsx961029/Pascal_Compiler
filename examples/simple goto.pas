label lab, restart;
var i: integer;

begin
    i := 1;

    restart:
        i := i+1;

        if i = 2 then
        begin
            goto lab;
        end;
        
        writeln('A');
        i := 100;

    lab: 
        writeln('B');
        writeln(i);
        if i = 2 then 
        begin
            goto restart;
        end;

end.
