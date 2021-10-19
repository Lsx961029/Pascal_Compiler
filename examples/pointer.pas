var 
    int: integer;
    float, sum: real;
    p1:^ integer;
    p2:^ real;

begin
    int := 200;
    writeln(int);
    
    p1:=@int;
    p1^:=100;

    writeln(int);
    writeln(p1^);
    writeln(p1);


    p2:=@float;
    float:=3.1415926;
    writeln(p2^);
    writeln(p2);

    sum := p2^+p1^;
    writeln(sum);

end.