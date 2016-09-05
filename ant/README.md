To use this build file, you need to define the library directories commons.dir (containing commons-lang3_version_.jar) and javatuples.dir (containing javatuples_version_.jar). I don't think it matters which version you choose, but I have built it with apache commons 3.1 and javatuples 1.2. 

You also need to put the one-jar task in your ant library. I'm not going to tell you where the library directory is, because I _suspect_ it might be ant-version dependent. You should search the internet for one-jar ant task: at the time of writing it can be loaded from [SourceForge](https://sourceforge.net/downloads/one-jar/one-jar/one-jar-0.97/one-jar-ant-task-0.97.jar). Then I'm not quite sure how to put the task into the library: the documentation is hard to read and I have misunderstood it many times. I have in my library a directory called one-jar-ant-task-0.97, which may be an unpacking of one-jar-ant-task-0.97.jar. I also have that jar (one-jar-ant-task-0.97.jar) in my ant lib directory, and it works.

After that, you say **ant compile** to compile the Java, **ant jarbuild** to build the program (which will also compile if necessary) and **ant run** to run it (which will also build, I hope). **ant clean** cleans up compilation files and also the program itself.

If it doesn't work for you, let me know.

Richard Bornat

richard@bornat.me.uk