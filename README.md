# Distributed Search Engine

## Overview

This project implements a distributed search engine where clients can perform several actions, including:

- **Search** for web pages.
- **Index** new web pages.
- **Query** the number of links pointing to a specific web page.

The system is composed of several distributed components:

- **Clients:** Users interact with the search engine through the client, sending requests to search, index, and query web pages.
- **Server:** The central hub that receives and processes requests from clients, managing communication with the distributed system components.
- **Barrels:** Distributed storage units responsible for holding the indexed data of web pages.
- **Queue:** A messaging queue that distributes requests from the server to the downloaders.
- **Downloaders:** Web crawlers that parse the web pages based on the requests, extracting data and sending it back to the barrels for storage.

## Architecture

1. **Client Request:** A client sends a request to the server to search, index, or query web pages.
2. **Server Processing:** The server receives the request and determines the appropriate action.
3. **Queue Management:** The request is placed in a queue, which manages the distribution of tasks to the downloaders.
4. **Downloaders:** The downloaders act as web crawlers, processing the requests by parsing the web pages, and collecting the necessary data.
5. **Barrels Storage:** Parsed data from the downloaders is sent to the barrels for indexing and storage.
6. **Recursive Storage:** If a web page contains links to other pages, these are also processed and stored recursively.
