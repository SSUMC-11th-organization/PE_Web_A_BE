import { ConfigService } from '@nestjs/config';
import * as mysql from 'mysql2/promise';

export const DATABASE_CONNECTION = 'DATABASE_CONNECTION';

export const databaseProviders = [
  {
    provide: DATABASE_CONNECTION,
    inject: [ConfigService],
    useFactory: (configService: ConfigService) =>
      mysql.createPool({
        host: configService.get<string>('DB_HOST', '127.0.0.1'),
        port: Number(configService.get<string>('DB_PORT', '3306')),
        user: configService.get<string>('DB_USER', 'root'),
        password: configService.get<string>('DB_PASSWORD', ''),
        database: configService.get<string>('DB_NAME', 'library_week1'),
        waitForConnections: true,
        connectionLimit: 10,
        queueLimit: 0,
      }),
  },
];
