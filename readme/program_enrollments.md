## Data Import: Domain 'programenrollments'
The **programenrollments** subfolder contains CSV import files for saving program enrollments
in bulk. This is a possible example of its contents:

```bash
data_import/
  └──programenrollments/
    ├──programenrollments.csv
    └── ...
```
Here are the possible headers with a sample data set.

| uuid                                 | Void/Retire | Date Enrolled        | Date Completed       | Person UUID                          | Program Name | Location | Outcome Concept |
|--------------------------------------|-------------|----------------------|----------------------|--------------------------------------|--------------|----------|-----------------|
| f2a345cc-ffbc-4350-aeef-10ba109cf924 |             | 2018-02-01T10:00:00Z | 2018-10-01T10:00:00Z | a03e395c-b881-49b7-b6fc-983f6bddc7fc | HIV PROGRAM  | Xanadu   | DIED            |

##### Specifying Concepts

Concepts can be referred to by reference term or by name.
A reference term
should be specified as `Source:Code`, e.g. `CIEL:12345`.
Concept names must be in the implementation's preferred locale.

##### Headers

###### Headers `Date Enrolled`, `Date Completed`
[ISO-8601](https://en.wikipedia.org/wiki/ISO_8601) formatted date-times.

Only `Date Enrolled` is required.

###### Header `Person UUID`
The UUID of the person/patient the observation is for.
If you are importing persons/patients
from CSV, this should match the their entry in the "uuid" column of the CSV
file.

###### Header `Program Name`
The canonical name of the program (not the name of the associated concept).

###### Header `Location`
The name of the location (the "login" or "session" location) where the
patient was enrolled.

###### Header `Outcome Concept`
A concept (see "Concepts" above). Should be set if and only if `Date Completed`
is present.

#### Further examples
Please see at the
[test folder](../api/src/test/resources/testAppDataDir/import_data)
for sample import files for all domains.

