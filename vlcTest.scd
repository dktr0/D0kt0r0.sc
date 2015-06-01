Vlc.meow(\nime2015);

~sines = { SinOsc.ar([60,64,67,71].midicps,mul:-20.dbamp) }
~sines.to(\stage);
~sines.to(\left);
~sines.to(\right);
~sines.to(\back);
~sines.to(\up);
~sines.to(\all);
~sines.stop(15);

Vlc.test;
Pbindef(\test,\out,Pdefn(\stage));
Pbindef(\test,\out,Pdefn(\left));
Pbindef(\test,\out,Pdefn(\right));
Pbindef(\test,\out,Pdefn(\back));
Pbindef(\test,\out,Pdefn(\up));
Pbindef(\test,\out,Pdefn(\all));
Pbindef(\test).stop;

~tabla = -20

Vlc.quit