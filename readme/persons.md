## Data Import: Domain 'persons'
The **persons** subfolder contains CSV import files for saving persons in bulk. This is a possible example of its content:
```bash
import_data/
  └──persons/
    ├──persons.csv
    └── ...
```
There is currently only one format for the patient CSV line, here are the possible headers with a sample data set:

| uuid                                 | Void/Retire | Given names     | Middle names | Family names | Gender | Birthdate  | Birthdate is estimated | Addresses                                                             |
|--------------------------------------|-------------|-----------------|--------------|--------------|--------|------------|------------------------|-----------------------------------------------------------------------|
| 86e140a6-30c3-4e47-8116-a44cfb7ba60f |             | Pippin,Peregrin |              | -,Took       | M      | 1980-02-01 | FALSE                  | cityVillage:The Shire,address1:Bag End;country:NZ,cityVillage:Hinuera |

###### Header `Given names`, `Middle names`, `Family names`
Comma-separated lists of names. For a given row, each entry must either be
empty or the same length as the other entries.

###### Header `Gender`
Per [this issue](https://issues.openmrs.org/browse/TRUNK-4832): `M` or `F`.

###### Header `Birthdate`
Should be formatted `yyyy-mm-dd`. Don't worry about zero-padding; `02` is the
same as `2`.

###### Header `Birthdate is estimated`
`TRUE` or `FALSE`.

###### Header `Addresses`
A semicolon-separated list of addresses. Each address is a comma-separated
list of address components specified as `Address Component Name:Value`.

e.g. `cityVillage:The Shire,address1:Bag End;country:NZ,cityVillage:Hinuera`

#### Further examples
Please see at the
[test folder](../api/src/test/resources/testAppDataDir/import_data)
for sample import files for all domains.
