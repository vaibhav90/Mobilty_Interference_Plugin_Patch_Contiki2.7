The Origianl Mobility and ewsn11_cooja_interference Plugins (Authors:Fredrik Osterlind, Marcus Lunden, Carlo Alberto Boano) were built on Contiki 2.4, 
after that the directory structure was changed , because of which these plugins were not getting built. 
Here are the working PLugins for Contiki 2.7 after making the necessary changes. I have not checked the functionality of the patch on Contiki 3.x, as 
a result I am not sure about the directory changes in 3.x. If you want to issue pull request to modify it for 3.x let me know


Make sure you change the Build.xml file path to link the Cooja.Jar file in Cooja_Jar Property.  
