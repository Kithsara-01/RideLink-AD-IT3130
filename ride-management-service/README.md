# Ride Management Service

Ride Management is the third RideLink microservice. It owns ride requests and the ride lifecycle; it does not read another service's database.

## Run

Set `MONGODB_URI_RIDE` and `JWT_SECRET` in the environment. `JWT_SECRET` must be a Base64-encoded key with at least 32 decoded bytes.

```bash
bash mvnw spring-boot:run
```

The service starts on `http://localhost:8083`. Configure the Driver & Vehicle Service URL with `DRIVER_SERVICE_URL` (default `http://localhost:8082`). Swagger UI is available at `/swagger-ui/index.html`.

## Endpoints

- `POST /api/rides` creates a ride request and stores pickup, destination, vehicle preference, and a distance-based fare estimate.
- `GET /api/rides/{id}` retrieves one ride.
- `GET /api/rides?passengerId={id}` lists a passenger's rides.
- `GET /api/rides?driverId={id}` lists a driver's assigned rides.
- `POST /api/rides/{id}/assign` finds eligible drivers through Driver & Vehicle Service and assigns the nearest result.
- `PATCH /api/rides/{id}/status` applies `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, or `CANCELLED` transitions.

Valid lifecycle: `REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED`. A ride can be cancelled from `REQUESTED`, `ASSIGNED`, `ACCEPTED`, or `IN_PROGRESS`; cancellation requires a reason.

## Fare estimate rule

The service uses a transparent simulated estimate for the ride snapshot: `2.50 + (great-circle distance in km * 1.20)`, rounded to two decimal places. Final fare is fixed to the stored estimate when a ride is completed; the Fare & Payment Service can replace this snapshot in the integrated workflow.

## Tests

```bash
bash mvnw test
```
