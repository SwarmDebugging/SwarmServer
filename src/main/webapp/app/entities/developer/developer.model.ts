import { BaseEntity } from './../../shared';

export class Developer implements BaseEntity {
    constructor(
        public id?: number,
        public username?: string,
        public password?: string,
        public name?: string,
        public sessions?: BaseEntity[],
    ) {
    }
}
