Authors:        Marco Zuehlke
                Ralf Quast

Affiliation:    BEAM Development Team
                Brockmann Consult GmbH
                Max-Planck-Strasse 1
                D-21502 Geesthacht, Germany
                www.brockmann-consult.de

E-mails:        marco.zuehlke@brockmann-consult.de

Document title: Brief Documentation of the MetopReader plugin
                A plug-in for MERIS/(A)ATSR Toolbox (BEAM)

Release date:	March 22, 2007
Last update:    October 31, 2014


Introduction
------------

This file briefly describes the installation and use of the MetopReader 
BEAM4 plug-in.


Requirements
------------

The MetopReader is a plug-in module for the BEAM software developed
by Brockmann Consult under contract to ESA. This latter software must of 
course be installed prior to the plugin.

This plug-in requires BEAM version 5.0 or higher.

The BEAM software includes an application programming interface (API) and
a set of executable tools to facilitate the use of MERIS, AATSR and further
ASAR data products of the ESA ENVISAT satellite.

It can be freely downloaded from:

http://www.brockmann-consult.de/beam/



Installation
------------

For the purpose of this documentation, the BEAM installation directory 
is referred to as $BEAM_HOME$. After installation of the BEAM software, 
this directory should contain the following subdirectories:

    $BEAM_HOME$
        |- bin/
        |- config/
        |- lib/
        |- licenses/
        |- modules/

The MetopReader plug-in, named 'beam-metop-avhrr-reader-1.0-SNAPSHOT.jar', must
simply be added to the $BEAM_HOME$/modules/ subdirectory. The VISAT application
will automatically integrate the plugin within its interface.



Operation
---------

After starting the VISAT application you will find the new menu entry
'METOP-AVHRR/3 Level-1b Product (or Subset)...' in the 'File' menu at the
'Import' section.

This plugin allows you to import METOP-AVHRR/3 products.

The specification for these data products can be found at the following location:
http://www.eumetsat.int/website/wcm/idc/idcplg?IdcService=GET_FILE&dDocName=PDF_AVHRR_L1B_PRODUCT_GUIDE&RevisionSelectionMethod=LatestReleased&Rendition=Web

General information about the METOP satellite can be found here:
http://www.eumetsat.int/idcplg?IdcService=SS_GET_PAGE&nodeId=47&l=en

Warranties and copyright information
------------------------------------

 Copyright (C) 2005 by EUMETSAT

The Licensee acknowledges that the AVISA software is owned by the European
Organisation for the Exploitation of Meteorological Satellites
(EUMETSAT) and the Licensee shall not transfer, assign, sub-licence,
reproduce or copy the AVISA software to any third party or part with
possession of this software or any part thereof in any way whatsoever.

This library is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.
The AVISA software has been developed using the ESA BEAM software which is
distributed under the GNU General Public License (GPL).
