## Data Import: Domain 'obs'
The **obs** subfolder contains CSV import files for saving observations
in bulk. This is a possible example of its contents:

```bash
import_data/
  └──obs/
    ├──obs.csv
    └── ...
```
Here are the possible headers with a sample data set. Here are demonstrated a
number of possible data types: numeric, boolean, coded, text, and date. The
use of concept sets is also demonstrated.

| uuid                                 | Date                 | Person UUID                          | Location | Encounter UUID                       | Concept                  | Value                | Void/Retire | Set Members                              | Set Member Values |
|--------------------------------------|----------------------|--------------------------------------|----------|--------------------------------------|--------------------------|----------------------|-------------|------------------------------------------|-------------------|
| 5a1634a5-ef99-48e3-919e-fcf2e6464ee3 | 2019-01-04T13:00:00Z | 5946f880-b197-400b-9caa-a3c661d23041 | Xanadu   | 6519d653-393b-4118-9c83-a3715b82d4ac | SNOMED CT:2332523        | 90                   |             |                                          |                   |
| ed88d046-a558-4112-a2fd-275ee4da4f66 | 2019-01-02T13:00:00Z | 5946f880-b197-400b-9caa-a3c661d23041 | Xanadu   |                                      | FOOD ASSISTANCE          | FALSE                |             |                                          |                   |
| 9fd15da4-27e6-43c7-a3cf-c718997926f4 |                      |                                      |          | 6519d653-393b-4118-9c83-a3715b82d4ac | CIVIL STATUS             | MARRIED              |             |                                          |                   |
| 734a37ea-cbb1-499c-ad01-5a07b752ab52 | 2019-01-02T13:00:00Z | 5946f880-b197-400b-9caa-a3c661d23041 | Xanadu   |                                      | FAVORITE FOOD, NON-CODED | Slim Jims            |             |                                          |                   |
| d346cd50-1506-4677-8f00-258cbbaa8d4f | 2019-01-02T13:00:00Z | 5946f880-b197-400b-9caa-a3c661d23041 | Xanadu   |                                      | DATE OF FOOD ASSISTANCE  | 2018-07-01T00:00:00Z |             |                                          |                   |
| fd93d046-a558-4112-a2fd-275ee4da4f66 | 2019-01-02T13:00:00Z | 5946f880-b197-400b-9caa-a3c661d23041 | Xanadu   |                                      | FOOD CONSTRUCT           |                      |             | FOOD ASSISTANCE;FAVORITE FOOD, NON-CODED | TRUE;Hot Pockets  |

##### Specifying Concepts

Concepts can be referred to by reference term or by name.
A reference term
should be specified as `Source:Code`, e.g. `CIEL:12345`.
Concept names must be in the implementation's preferred locale.

##### Headers

###### Header `Date`
The date of the observation. An
[ISO-8601](https://en.wikipedia.org/wiki/ISO_8601) formatted date-time.

Defaults to the date of the encounter, if `Encounter UUID` is provided.

###### Header `Patient UUID`
The UUID of the patient the observation is for.
If you are importing patients
from CSV, this should match the patient's entry in the "uuid" column of the
patients CSV file.

Defaults to the patient associated with the encounter, if `Encounter UUID` is provided.

###### Header `Location`
The name of the location (the "login" or "session" location) where the
observation occurred.

Defaults to the location of the encounter, if `Encounter UUID` is provided.

###### Header `Encounter UUID`
The UUID of the encounter in which this observation occurred.
If you are importing encounters
from CSV, this should match the encounter's entry in the "uuid" column of the
encounter CSV file.

###### Header `Concept`
A concept (see "Specifying Concepts" above), which is either
the question that this observation provides information about, or
a set.

> Note: Observation Values
>
> If `Concept` is not a set, then this observation should have a single value,
> which should be specified in `Value`.
>
> If `Concept` is a set, then this observation should contain a number of member
> concepts and their values. The `Value` column should be left empty.

###### Header `Value`
Only if `Concept` is not a set. The acceptable format for this value depends on
the datatype of `Concept`.

- `Numeric` should be a number, like `130` or `4.55`.

- `Boolean` should be `TRUE` or `FALSE`.

- `Coded` should be a concept.

- `Text` can be anything, it will be interpreted as a string.

- `Date` should be an [ISO-8601](https://en.wikipedia.org/wiki/ISO_8601) formatted date-time.

###### Header `Set Members`
A semicolon-delimited list of concepts, which are the members of the set
named in `Concept`.

###### Header `Set Member Values`
A semicolon-delimited list of values. The acceptable format for each value
depends on the datatype of the corresponding concept in `Set Members`. Please
see "Header `Value`" above for the specification.

#### Further examples
Please see at the
[test folder](../api/src/test/resources/testAppDataDir/import_data)
for sample import files for all domains.

