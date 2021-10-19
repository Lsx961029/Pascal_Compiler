var f, a, c: real;
var e, b, d: integer; 
var letter: char;
    t: boolean;

begin
    t:= true;
    writeln(t);

    letter := 'i';
    writeln(letter);
    
    a := 33/11.0;
    c := 4/a;
    writeln(c);

    b := 3;
    d := -4;
    c := b/d;
    writeln(c);

    e:= d div b;
    writeln(e);

    f := a;
    writeln(f);
    
    writeln(false);
    writeln('d');
    writeln(' ');

    f:= -c + 1.01;
    writeln(f);
end.