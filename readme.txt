MeshCMS Readme

------------------------------------------------------------------------------
This file contains some basic information about MeshCMS. For a more detailed description and guide, open the help/index.html file.
------------------------------------------------------------------------------

MeshCMS is a content management system written in Java. It provides the ability to edit a page using a web browser, then updates all navigation components automatically. It doesn't use databases: the site is file-based, so one can choose to make changes using MeshCMS or using local tools, like Dreamweaver, Notepad and so on.

MeshCMS is released under the GNU General Public License. See license.txt for details.

----------------------
0. Contents
----------------------

1. Installing MeshCMS
2. Using MeshCMS
3. Credits

----------------------
1. Installing MeshCMS
----------------------

Two .WAR files are provided: samples.war and meshcms.war. The former is more suitable for testing, since it contains some sample pages and themes. The latter is recommended for usage in production, and to upgrade from a previous version. Both files are ready to be deployed on your application server as usual.

----------------------
2. Using MeshCMS
----------------------

Open your new web application (usually http://localhost:8080/samples/ or http://localhost:8080/meshcms/). You can login using username "admin" and password "admin".

----------------------
3. Credits
----------------------
MeshCMS uses free software from other companies/developers:

- SiteMesh
    http://www.opensymphony.com/sitemesh/

- TinyMCE
    http://tinymce.moxiecode.com/

- XTree, XMenu
    http://webfx.eae.net/

- Jakarta Commons FileUpload
    http://jakarta.apache.org/commons/fileupload/

- JavaMail
    http://java.sun.com/products/javamail/

- XStream
    http://xstream.codehaus.org/

- jcrypt (slightly modified)
    http://locutus.kingwoodcable.com/jfd/crypt.html

- FamFamFam (icons)
    http://www.famfamfam.com/

- ALIB (modified by Hue Holleran)
    http://sourceforge.net/projects/alib/

- EditArea
    http://www.cdolivet.net/index.php?page=editArea

- SWFObject
    http://blog.deconcept.com/swfobject/

- XSPF Web Music Player
    http://musicplayer.sourceforge.net/

- FlowPlayer
    http://flowplayer.sourceforge.net/

- Tigra Menu & Tigra Tree Menu (not included, but supported)
    http://www.softcomplex.com/

Other tools are used in the build and development process:

- SVNAnt
    http://subclipse.tigris.org

- Tag Library Documentation Generator
    https://taglibrarydoc.dev.java.net/

- Checkstyle
    http://checkstyle.sourceforge.net/
