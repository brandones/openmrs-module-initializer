## Data Import: Domain 'encounters'
The **encounters** subfolder contains CSV import files for saving encounters
in bulk.

**Note** that when saving encounters, you will almost certainly want to create
visits for those encounters afterward. Instructions for doing so can be found
[here](https://wiki.openmrs.org/display/docs/How+to+create+visits+for+preexisting+encounters).

This is a possible example of content of the `data_import/encounters` folder.
```bash
data_import/
  └──encounters/
    ├──encounters.csv
    └── ...
```
Here are the possible headers with a sample data set:

| uuid                                 | Void/Retire | Date                 | Patient UUID                         | Location | Encounter Type | Form UUID                            |
|--------------------------------------|-------------|----------------------|--------------------------------------|----------|----------------|--------------------------------------|
| b330f4de-ed7d-4e9d-9ff3-81997e729aa8 |             | 2018-02-01T10:00:00Z | a03e395c-b881-49b7-b6fc-983f6bddc7fc | Xanadu   | Scheduled      | d9218f76-6c39-45f4-8efa-4c5c6c199f50 |


###### Header `Date`
The date of the encounter. An
[ISO-8601](https://en.wikipedia.org/wiki/ISO_8601) formatted date-time.

###### Header `Patient UUID`
The UUID of the patient the encounter is for. If you are importing patients
from CSV, this should match the patient's entry in the "uuid" column of the
patients CSV file.

###### Header `Location`
The name of the location (the "login" or "session" location) where the
encounter occurred.

###### Header `Encounter Type`
The name of the OpenMRS encounter type.

###### Header `Form`
The UUID of the Form to which this encounter corresponds. If omitted,
observations can still be attached to the patient/encounter, but it
will not be possible to view those observations in historical form
entries.

#### Further examples
Please see at the
[test folder](../api/src/test/resources/testAppDataDir/import_data)
for sample import files for all domains.
