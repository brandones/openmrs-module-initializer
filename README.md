# OpenMRS Initializer module
### Introduction
The Initializer module is an API-only module that processes the contents of
the **configuration** and **import_data** folders when they are found inside
OpenMRS's application data directory:

<pre>
.
├── modules/
├── openmrs.war
├── openmrs-runtime.properties
├── ...
├── <b>configuration/</b>
└── <b>import_data/</b>
</pre>
Each is subdivided into _domain_ subfolders:
```bash
configuration/
  ├── addresshierarchy/
  ├── concepts/
  ├── drugs/
  ├── globalproperties/
  ├── idgen/
  ├── jsonkeyvalues/
  ├── locations/
  ├── messageproperties/
  ├── metadatasharing/ 
  └── personattributetypes/
import_data/
  ├── encounters/
  ├── obs/
  ├── patients/
  ├── persons/
  └── programenrollments/
```  

The specifications for the files that are expected in these subfolders are
in the [readme](readme) directory.

The domains in `configuration` are metadata and, as you might expect,
configuration. Although several
file types are supported for providing metadata, CSV files are the preferred
format and all domains should aim at being covered through parsing CSV files.

The domains in `import_data` are used to load clinical data into OpenMRS. This
is useful, for example, for migrating old medical records from a legacy
system to OpenMRS.

### Objectives
* This module allows preloading an OpenMRS installation with **maintained and versioned metadata**.
* It also allows preloading an OpenMRS installation with **preexisting medical records**.
* It processes **CSV files** upon startup.
* Each line of those CSV files represents an **OpenMRS object to be created or edited**.

Even though using CSV files is the preferred approach, some data or metadata
domains rely on other file formats to be imported. You will encounter those
other formats in the list below.

### Supported domains and loading order
We suggest to go through the following before looking at specific import domains:
* [Conventions for CSV files](readme/csv_conventions.md)

This is the list of currently supported configuration domains, in their loading order:
1. [Message properties key-values (.properties files)](readme/messageproperties.md)
1. [Generic JSON key-values (JSON files)](readme/jsonkeyvalues.md)
1. [Metadatasharing packages (ZIP files)](readme/mds.md)
1. [Global properties (XML files)](readme/globalproperties.md)
1. [Locations (CSV files)](readme/loc.md)
1. [Concepts (CSV files)](readme/concepts.md)
1. [Person attribute types (CSV files)](readme/pat.md)
1. [Identifier sources (CSV files)](readme/idgen.md)
1. [Drugs (CSV files)](readme/drugs.md)
1. [Order Frequencies(CSV files)](readme/freqs.md)

This is the list of currently supported import data domains, in their loading order:
1. [Persons](readme/persons.md)
1. [Patients](readme/patients.md)
1. [Encounters](readme/encounters.md)
1. [Observations](readme/obs.md)
1. [Program Enrollments](readme/programenrollments.md)

### How to try it out
Build it and install the built OMOD to your OpenMRS instance:
```bash
git clone https://github.com/mekomsolutions/openmrs-module-initializer
cd openmrs-module-initializer
mvn clean install
```

### How to load data
There are a number of considerations particular to using Initializer to load
clinical data into OpenMRS.

Before you begin, if you are not yet familiar with the OpenMRS data model,
you should
[give this a read](https://guide.openmrs.org/en/Getting%20Started/openmrs-information-model.html).

You can attempt to transform all your clinical data into
the correct format at once, or do it piecewise. If you do it piecewise, be
sure to do it in an order which respects the dependencies of the OpenMRS data
model: observations depend on encounters which depend on patients, and
program enrollments depend on patients.

You will probably not succeed in getting all your data in on the first try,
for any given domain.
That's no problem since Initializer will ignore or update already-existing
entities with the same UUID. But this means that if you are
doing data transformation programatically, you must make sure your UUID
generation algorithm is deterministic and unlikely to change for a particular
entity. Otherwise you'll probably have to wipe the database and reload from
scratch.

An example application for transforming data programatically is
[this one](https://github.com/PIH/ces-data-migration), which was
built in R for PIH Mexico. It may be a helpful reference if you're
thinking of doing the transformation programatically; however, having
written that, I would strongly recommend against using R for this, in
favor of something like Python with Pandas.

With an Intel Core i7-8550U, 16GB DDR4 RAM, and an SSD, patients were loaded at
a rate of 10-14 per second. Encounters and observations were each loaded at
rates of about 100 per second. Your mileage may vary.

##### Runtime requirements & compatibility
* Core 1.11.9

### Quick facts
Initializer enables to achieve the OpenMRS backend equivalent of Bahmni Config for Bahmni Apps. It facilitates the deployment of implementation-specific configurations without writing any code, by just filling the **configuration** folder with the needed metadata and in accordance to Initializer's available implementations.

### Get in touch
Find us on [OpenMRS Talk](https://talk.openmrs.org/): sign up, start a conversation and ping us with the mentions starting with @mks.. in your message.

----

### Releases notes

#### Version 1.1.0
* Bulk creation and edition of drugs provided through CSV files in  **configuration/locations**.

#### Version 1.0.1
* Loads i18n messages files from **configuration/addresshierarchy** and **configuration/messageproperties**.
* Bulk creation and edition of concepts provided through CSV files in  **configuration/concepts**.<br/>This covers: basic concepts, concepts with nested members or answers and concepts with multiple mappings.
* Bulk creation and edition of drugs provided through CSV files in  **configuration/drugs**.
* Overrides global properties provided through XML configuration files in **configuration/globalproperties**.
* Modifies (retire) or create identifier sources through CSV files in **configuration/idgen**.
* Exposes runtime key-values configuration parameters through JSON files in **configuration/jsonkeyvalues**.
* Bulk creation and edition of person attribute types provided through CSV files in  **configuration/personattributetypes**.
* Imports MDS packages provided as .zip files in **configuration/metadatasharing**.
* Bulk creation and edition of order frequencies provided through CSV files in  **configuration/orderfrequencies**.
