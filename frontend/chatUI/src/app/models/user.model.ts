export interface User {
  id: number;
  email: string;
  name: string;
  role: 'ROLE_USER' | 'ROLE_AGENCY_STAFF';
}
