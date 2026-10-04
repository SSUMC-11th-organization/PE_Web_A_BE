import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { BookController } from './book.controller';
import { BookRepository } from './book.repository';
import { BookService } from './book.service';
import { databaseProviders } from './database.provider';
import { RentalController } from './rental.controller';
import { RentalRepository } from './rental.repository';
import { RentalService } from './rental.service';

@Module({
  imports: [ConfigModule.forRoot({ isGlobal: true })],
  controllers: [BookController, RentalController],
  providers: [
    ...databaseProviders,
    BookRepository,
    BookService,
    RentalRepository,
    RentalService,
  ],
  exports: [...databaseProviders],
})
export class AppModule {}
