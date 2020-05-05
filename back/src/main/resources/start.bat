for /f "delims=" %%A in ('dir /b *.jar') do set "filename=%%A"
title %filename%
start java -Dloader.path=. -jar -Djava.ext.dirs=lib %filename% --debug=false