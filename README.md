not gonna write much here, but this is a Java interpreter for Brainfuck. It's rather fast (faster than my python one) but wasn't written to be readable, or user friendly if you don't know how to code. I might change that later, but for now this will do.

If you want to see how the pointer moves about the memory, replace the `Interpreter` constructor call in `App.java` to include `true` as a parameter. Make sure that your console is wide enough to view the whole thing otherwise it's gonna get real fucked up.

And one more thing; it's always going to interpret `brainfuck.bf` (unless you change that). By default it's the mandlebrot set, but if you want it to be your own code or something else I suggest changing the contenst of that file.

Also, don't trust my code. I'm not that good a java dev.