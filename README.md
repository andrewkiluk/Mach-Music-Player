Simple-Music-Player
===================

The music apps I've found for Android have either been buggy or have had bad interfaces, so my first attempt at an app
is making a music player that I like. 

Version 0.8:
The player is mostly working, but with the following issues:

 - Repeat is not working.
      I'm not sure what's causing this.

 - Buttons for shuffle and repeat need to look pushed down when activated.
      This is just cosmetic.

 - Album art does not change with the song.
      This probably requires copying some code to a different class.

 - The player only loads music from the directory /sdcard/media/audio/mp3.
      This is the big issue. It needs to, at the minimum, also scan subdirectories. Eventually the user should be 
      able to specify directories to scan.
      
I think once these issues are addressed it will have earned the version 1.0.
