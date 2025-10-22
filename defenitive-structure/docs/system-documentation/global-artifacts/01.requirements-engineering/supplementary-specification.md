# Supplementary Specification (FURPS+)

## Functionality

_Specifies functionalities that:  
&nbsp; &nbsp; (i) are common across several US/UC;  
&nbsp; &nbsp; (ii) are not related to US/UC, namely: Audit, Reporting, and Security._

- **Authentication and Authorization**: Ensuring that users have the correct permissions to access specific functions.
- **Logging and Audit Trails**: Recording system activities for tracking changes and ensuring resilience.
- **Data Backup**: Automating backups to prevent data loss and system failures.
- **System Notifications**: Sending alerts about unusual or critical events from user actions.

## Usability

_Evaluates the user interface. It has several subcategories, among them: error prevention; interface aesthetics and design; help and documentation; consistency and standards._

- **Intuitive Navigation**: Ensuring that features and menus are easy to find, access, and use.
- **Errors and Recovery**: Show clear error messages and a way to solve them.
- **Comprehensive Documentation**: Offering tutorials, explanations, and clear information to assist users.
- **Accessibility Compliance**: Supporting users with disabilities through high-contrast modes, keyboard navigation, and screen readers.

## Reliability

_Refers to the integrity, compliance, and interoperability of the software. The requirements to be considered are: frequency and severity of failure, possibility of recovery, possibility of prediction, accuracy, average time between failures._

- **Data Consistency**: Ensure that data remains accurate and up to date across all modules.
- **Error Detection**: Identify and solve errors before they affect some functionality.
- **Disaster Recovery Planning**: Preparing for unexpected failures and errors with recovery protocols.
- **Automated Monitoring**: Tools to track system performance and detect issues that could affect functionality.
- **Failover Mechanisms**: Implementing redundancy to prevent system downtime.

## Performance

_Evaluates the performance requirements of the software, namely: response time, start-up time, recovery time, memory consumption, CPU usage, load capacity, and application availability._

- **Optimized Query Processing**: Reducing the time the database takes to execute a command.
- **Efficient Resource Allocation**: Balancing memory and CPU usage to avoid obstructions.
- **Scalability Testing**: Ensuring the system can handle increased load scenarios.
- **Fast System Startup**: Minimizing downtime and ensuring quick restart times.
- **Load Balancing**: Distribute traffic to maintain consistent response times.

## Supportability

_The supportability requirements gather several characteristics, such as testability, adaptability, maintainability, compatibility, configurability, installability, scalability, and more._

- **Modular Architecture**: Facilitating modifications or updates without affecting the entire system.
- **Automated Testing**: Providing easy execution of integrated tests.
- **Configuration Management**: Allow settings to be adjusted without requiring code changes.
- **Backward Compatibility**: Ensuring new versions work with existing data and configurations.
- **Cross-Platform Compatibility**: Support for multiple operating systems and environments.

## +

### Design Constraints

_Specifies or constrains the system design process. Examples may include: programming languages, software process, mandatory standards/patterns, use of development tools, class library, etc._

- **Use of MVC Architecture**: Ensuring a clear separation of concerns in development.
- **Version Control**: Mandating GitHub usage for collaborative development.
- **Code Documentation**: Require comments and documentation on developed code functions (e.g., Javadoc for Java).
- **Predefined Development Frameworks**: Use of Test-Driven Development (TDD) approach.

### Implementation Constraints

_Specifies or constrains the code or construction of a system such as: mandatory standards/patterns, implementation languages, database integrity, resource limits, operating system._

- **Programming Languages**: Java, SQL, C/Assembly.
- **Database Integrity**: The database will be the main repository of information for the system and must reflect necessary data integrity, maintained in a remote database management system.
- **Security Protocols**: Basic input validation to avoid unsafe practices; no specific security protocols for sensitive data or network communication.
- **Resource Limits**: The system should run efficiently on standard machines with at least 4 GB of RAM and a dual-core processor, avoiding excessive CPU or memory usage.
- **Operating System Compatibility**: Windows, MacOS, and Linux.

### Interface Constraints

_Specifies or constrains the features inherent to the interaction of the system being developed with other external systems._

- **API Standardization**: Not applicable. All components are internal and tightly coupled.
- **Data Format Compliance**: The system uses standard Java data types and CSV files for internal data handling; no compliance with external data formats is required.
- **Latency Requirements**: As a standalone application with local execution, there are no strict latency requirements; the system should respond promptly to user inputs under normal usage.
- **Authentication Methods**: Basic username and password authentication mechanism to restrict access, handled locally without external identity providers.

### Physical Constraints

_Specifies a limitation or physical requirement regarding the hardware used to house the system, as for example, material, shape, size, or weight._

- **Hardware Specifications**: Designed to run on standard personal computers or laptops.
- **Storage Requirements**: Minimal disk space required, including all necessary files and dependencies.
- **Devices Compatibility**: Compatible with desktop and laptop computers; mobile devices and tablets are not supported.
- **Power Considerations**: Runs on general-purpose computing devices without additional power consumption constraints.