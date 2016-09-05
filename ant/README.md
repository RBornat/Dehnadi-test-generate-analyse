To use this build file, you need to define the library directories commons.dir (containing commons-lang3_version_.jar) and javatuples.dir (containing javatuples_version_.jar). I don't think it matters which version you choose, but I have built it with apache commons 3.1 and javatuples 1.2. 

You also need to put the one-jar task in your ant library. I'm not going to tell you where the library directory is, because I _suspect_ it might be ant-version dependent. You should search the internet for one-jar ant task: at the time of writing it can be loaded from [SourceForge](https://sourceforge.net/projects/one-jar/files/one-jar/one-jar-0.97/one-jar-ant-task-0.97.jar/download). The file you want is one-jar-ant-task-_version_.jar_ -- accept no substitutes. 

Then I'm not quite sure how to put the one-jar task into the library: the documentation is hard to read and I have misunderstood it many times. It seems to be enough to put one-jar-ant-task-0.97.jar in the ant lib directory. I have that, and it works.

After that, you say **ant compile** to compile the Java, **ant build** to build the program (which will also compile if necessary) and **ant run** to run it (which will also build if necessary, and of course compile as well). 

**ant clean** cleans up compilation files and also the program itself.

If it doesn't work for you, let me know.

Richard Bornat

richard@bornat.me.uk