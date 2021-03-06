import { BaseEntity } from './../../shared';

export class Project implements BaseEntity {
    constructor(
        public id?: number,
        public name?: string,
        public description?: string,
        public url?: string,
        public tasks?: BaseEntity[],
    ) {
    }
}
