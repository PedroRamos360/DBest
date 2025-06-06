<div align="left">
    <a href="./07 - working-with-CSV.md">Previous</a>
</div>
<div align="right">
  <a href="./09 - understanding-schemas.md">Next</a>
</div>

# Working with Temporary Tables in DBest

DBest provides support for **temporary tables**, offering a quick way to generate data for use within a query tree.

## Creating a Temporary Table
1. **Access the "Create Table" Option**:
   - Navigate to the **bottom menu** and select **"Create Table"**.
   - Enter a name for your temporary table.

2. **Define Columns**:
   - Use the **"Add Column"** button to specify the column name and data type for each column.
   - Example: Create a `Person` table with the following columns:
     - `name` (string)
     - `age` (integer)

3. **Add Rows**:
   - Click the **"Add Row"** button to create rows. Rows are initialized with empty content.

4. **Generate Data**:
   - Select the **"Generate Data"** button to define the content for each column.
   - The data is generated **one column at a time**, meaning you need to repeat this step for each column.

### Example: Defining Data for the "Age" Column
The screenshot below illustrates data generation for the `Age` column:

![Data Generation Example](assets/images/temp-table.png)

Repeat the **Generate Data** process for all columns to complete the table's content.

## Using the Temporary Table
Once the table is created:
- It will appear as a **temp table node** in the **left-side panel**.
- The table is now ready to be used within your query tree.

Temporary tables in DBest make it easy to experiment with structured data without relying on external sources.

<br>

<div align="left">
    <a href="./07 - working-with-CSV.md">Previous</a>
</div>
<div align="right">
  <a href="./09 - understanding-schemas.md">Next</a>
</div>
