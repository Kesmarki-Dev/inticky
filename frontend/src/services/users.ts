import { api } from './api';
import type { UserResponse, UserCreateRequest, UserUpdateRequest, PaginatedResponse } from '../types/api';

export const getUsers = async (page = 0, size = 20): Promise<PaginatedResponse<UserResponse>> => {
  const response = await api.get(`/users?page=${page}&size=${size}`);
  return response.data;
};

export const getUserById = async (id: string): Promise<UserResponse> => {
  const response = await api.get(`/users/${id}`);
  return response.data;
};

export const getUserByEmail = async (email: string): Promise<UserResponse> => {
  const response = await api.get(`/users/email/${email}`);
  return response.data;
};

export const searchUsers = async (query: string, page = 0, size = 20): Promise<PaginatedResponse<UserResponse>> => {
  const response = await api.get(`/users/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  return response.data;
};

export const getUsersByRole = async (roleName: string, page = 0, size = 20): Promise<PaginatedResponse<UserResponse>> => {
  const response = await api.get(`/users/role/${roleName}?page=${page}&size=${size}`);
  return response.data;
};

export const createUser = async (userData: UserCreateRequest): Promise<UserResponse> => {
  const response = await api.post('/users', userData);
  return response.data;
};

export const updateUser = async (id: string, userData: UserUpdateRequest): Promise<UserResponse> => {
  const response = await api.put(`/users/${id}`, userData);
  return response.data;
};

export const updateUserStatus = async (id: string, status: string): Promise<UserResponse> => {
  const response = await api.put(`/users/${id}/status`, { status });
  return response.data;
};

export const updateUserRoles = async (id: string, roleIds: string[]): Promise<UserResponse> => {
  const response = await api.put(`/users/${id}/roles`, { roleIds });
  return response.data;
};

export const deleteUser = async (id: string): Promise<void> => {
  await api.delete(`/users/${id}`);
};

export const getUserStats = async () => {
  const response = await api.get('/users/tenant-stats');
  return response.data;
};