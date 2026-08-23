# SportX backend deployment configuration

Set these environment variables in the deployment environment. Do not commit
their values to source control.

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL for the production MySQL database. |
| `SPRING_DATASOURCE_USERNAME` | MySQL username. |
| `SPRING_DATASOURCE_PASSWORD` | MySQL password. |
| `APP_JWT_SECRET` | A strong, deployment-specific JWT signing secret. |
| `RAZORPAY_KEY_ID` | Razorpay publishable key for the intended environment. |
| `RAZORPAY_KEY_SECRET` | Razorpay server-side secret. Never expose this to the frontend. |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated browser origins allowed to call the API, such as `https://shop.example.com`. |
| `APP_UPLOAD_DIR` | Absolute path to durable, writable product-image storage. |

The frontend uses same-origin `/api/v1` and `/uploads` paths. Configure the
production reverse proxy to route both to the backend, or set an equivalent
same-origin deployment arrangement.

`APP_UPLOAD_DIR` must be persistent storage (for example, a mounted volume or
object-storage-backed mount) and the existing `backend/uploads` data must be
migrated before deployment. It is deliberately excluded from Git and is not
packaged inside the backend JAR.
