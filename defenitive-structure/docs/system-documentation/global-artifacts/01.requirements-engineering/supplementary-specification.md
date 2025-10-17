# Supplementary Specification (FURPS+)

## Functionality

_Specifies functionalities that:  
&nbsp; &nbsp; (i) are common across several US/UC;  
&nbsp; &nbsp; (ii) are not related to US/UC, namely: Audit, Reporting and Security._

- **Authentications and Authorization**: Ensuring that users have the correct permissions to access specific functions.
- **Logging and Audit Trails**: Recording system activities fot tracking the changes that are made on the system and ensuring resilience.
- **Multilingual Support**: Providing language options for different users regions.
- **Data Backup**: Automating backups to prevent data loss and system fails.
- **Systems Notifications**: Sending alerts about unusual or critical events from users actions.

## Usability

_Evaluates the user interface. It has several subcategories,
among them: error prevention; interface aesthetics and design; help and
documentation; consistency and standards._

- **Intuitive Navigation**: Ensuring that features and menus are easy to find, access and use.
- **Errors and Recovery**: Show clear error messages and a way to solve them.
- **User Customization**: Allow users to customize their experience, by changing themes, settings and layouts.
- **Comprehensive Documentation**: Offering tutorials, explanations and clear information to assist users.
- **Accessibility Compliance**: Supporting users with disabilities through high-contrast modes, keyboard navigation and screen readers.

## Reliability

_Refers to the integrity, compliance and interoperability of the software. The requirements to be considered are: frequency and severity of failure, possibility of recovery, possibility of prediction, accuracy, average time between failures._

- **Data Consistency**: Ensure that data remains accurate and up to date across all modules.
- **Error Detection**: Identify and solve errors before they affect some functionality.
- **Disaster Recovery Planning**: Preparing for unexpected failures and errors with recovery protocols.
- **Automated Monitoring**: Tools to track system performance and detect issues that could affect the functionality of the system.
- **Failover Mechanisms**: Implanting redundancy to prevent system downtime.

## Performance

_Evaluates the performance requirements of the software, namely: response time, start-up time, recovery time, memory consumption, CPU usage, load capacity and application availability._

- **Optimized Query Processing**: Reducing the time that the database takes to execute a command.
- **Efficient Resource Allocation**: Balancing memory and CPU usage to avoid obstructions.
- **Scalability Testing**: Ensuring that the system can handle long load screens.
- **Fast System Startup**: Minimizing downtime and have quick restart times.
- **Load Balancing**: Distribute traffic to maintain consistent response times.

## Supportability

_The supportability requirements gathers several characteristics, such as:
testability, adaptability, maintainability, compatibility,
configurability, installability, scalability and more._

- **Modular Architecture**: Facilitating modifications or updates without affecting the entire system.
- **Automated Testing**: Providing easy execution of integrated tests.
- **Configuration Management**: Allow that settings are adjusted without requiring code changes.
- **Backward Compatibility**: Make sure that new versions work with the existing data and configuration.
- **Cross-Platform Compatibility**: Ensure that supports multiple operating systems and environments.
## +

### Design Constraints

_Specifies or constraints the system design process. Examples may include: programming languages, software process, mandatory standards/patterns, use of development tools, class library, etc._

- **Use of MVC Architecture**: Ensuring a clear separation of the concerns on development.
- **Version Control**: Mandating GitHub usage, for collaborative development.
- **Code Documentation**: Require comments and documentation on the developed code functions.
- **Predefined Development Frameworks**: ...

### Implementation Constraints

_Specifies or constraints the code or construction of a system such
such as: mandatory standards/patterns, implementation languages,
database integrity, resource limits, operating system._

- **Programming Language**: Java 
- **Database Integrity**: Not applicable. The system does not use any database, so no data persistence or integrity constraints are required.
- **Security Protocols**: No specific security protocols will be implemented, as the system does not handle sensitive data or network communication. Basic input validation will be considered to avoid unsafe practices.
- **Resource Limits**: The system should run efficiently on standard machines with at least 4 GB of RAM and a dual-core processor. It must not require excessive CPU or memory usage.
- **Operating System Compatibility**: Windows, MacOS and Linux

### Interface Constraints

_Specifies or constraints the features inherent to the interaction of the
system being developed with other external systems._

- **API Standardization**: Not applicable. The system does not expose or consume any external APIs. All components are internal and tightly coupled.
- **Data Format Compliance**: The system uses standard Java data types for all internal data handling. No compliance with external data formats is required.
- **Latency Requirements**: As a standalone application with local execution, there are no strict latency requirements. The system should respond promptly to user inputs under normal usage.
- **Authentication Methods**: The system includes a basic username and password authentication mechanism to restrict access. User credentials are handled locally without external identity providers.

### Physical Constraints

_Specifies a limitation or physical requirement regarding the hardware used to house the system, as for example: material, shape, size or weight._

- **Hardware Specifications**: The system is designed to run on standard personal computers or laptops.
- **Storage Requirements**:  The application requires minimal disk space, including all necessary files and dependencies.
- **Devices Compatibility**: The system is compatible with desktop and laptop computers. Mobile devices and tablets are not supported.
- **Power Considerations**: The system runs on general-purpose computing devices and does not introduce additional power consumption constraints.