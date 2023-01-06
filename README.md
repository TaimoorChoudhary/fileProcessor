# File Processor
## Problem description
You should develop a small application that receive files as parameter, extract its content, 
outputting the results in a different file.

## Running the application
The application can be run using the following commands:  
- Maven is installed locally: ```mvn spring-boot:run```  
- Maven is not installed: ```./mvnw spring-boot:run```  

Alternatively the application can be executed using the provided docker file.

## Architecture
The application uses Spring Batch partition processing to read input files in parallel and extracts
required information. Two simple rest end-points are available which allow uploading txt file for processing and 
retrieving the file summary of a processed file.

### Decision-making
Spring bath job is selected to process files as it offers functionality to process files in a parallel manner
without the need for the developer to manage threads, thus reducing manual coding complexity. The batch job can be
scalled up depending on application requirements. As the application grows and the data set increases spring batch job's
logging or tracing, transaction management, job processing statistics functionality comes in handy.  
With Spring batch job's step execution reader, processor and writer classes are separated which makes it easier to
implement different types of readers and writers if the data source and data target changes. Also adding a new step 
during job execution is significantly easier as spring batch job allows adding custom listeners and skipping mechanisms 
making it highly customizable.

### Summary
The batch job starts when the application runs and is triggered at fixed intervals using a cron expression. 
The batch job reads file from the input folder extracts information and saves the summary in a new file in output folder.
If there are any errors during file processing the input file which has errors is moved to another folder 
and that specific file is stopped processing. After the processing is complete all the input files which 
were successfully processed are removed from the input folder.  

### Reader
Batch reader reads the input file line by line and creates model objects based on the format codes, 
if an unknown format value is encountered the reader class throws an exception and stops the file parsing.  

### Processor
The batch job processor is responsible for extracting the required information from the incoming parsed line.
It calculates the number of sellers, number of clients, highest sales id and the seller name with the lowest sales.  

### Writer
The batch job writer is responsible to write the summary extracted by the batch job processor. It writes a 
single line summary in a new file under the output folder where ".done" is appended to the original file name.

### File cleanup
Once the file processing step is complete a file cleanup step is executed as a last step of the batch job.
In this step all the files which are successfully processed are removed from the input folder and any file
which had a parsing error is moved to an error folder.

### Scheduled parsing
The batch job is scheduled to be executed are regular interval using a cron expression, whose value can be 
changed in the properties file.

### Adding files
Files can be directly added to the input directory where they are picked up during scheduled processing. Alternatively
files cane be uploaded using the upload rest end-point which places the file in the input directory

### Viewing file summary
A simple GET end-point is available which take file name (input file) as a parameter and return the file summary if it is available.