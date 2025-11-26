import React, { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { NavLink, useLocation } from 'react-router-dom';
import { 
  XMarkIcon,
  HomeIcon,
  TicketIcon,
  UserGroupIcon,
  BuildingOfficeIcon,
  ChartBarIcon,
  Cog6ToothIcon,
  ChatBubbleLeftRightIcon,
  BellIcon,
  DocumentTextIcon
} from '@heroicons/react/24/outline';
import { useAuth } from '../../hooks/useAuth';
import { cn } from '../../utils/cn';

interface NavigationItem {
  name: string;
  href: string;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  roles?: string[];
  permissions?: string[];
  badge?: string | number;
}

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ open, onClose }) => {
  const { hasAnyRole, hasPermission, isSystemAdmin, isTenantAdmin, isAgent } = useAuth();
  const location = useLocation();

  // Navigation items based on user roles
  const navigation: NavigationItem[] = [
    {
      name: 'Dashboard',
      href: '/dashboard',
      icon: HomeIcon,
    },
    {
      name: 'Tickets',
      href: '/tickets',
      icon: TicketIcon,
    },
    {
      name: 'My Tickets',
      href: '/my-tickets',
      icon: DocumentTextIcon,
    },
    {
      name: 'AI Chat',
      href: '/ai-chat',
      icon: ChatBubbleLeftRightIcon,
    },
    // Agent and Admin features
    {
      name: 'Users',
      href: '/admin/users',
      icon: UserGroupIcon,
      roles: ['TENANT_ADMIN', 'SYSTEM_ADMIN'],
    },
    // System Admin only
    {
      name: 'Tenants',
      href: '/admin/tenants',
      icon: BuildingOfficeIcon,
      roles: ['SYSTEM_ADMIN'],
    },
    {
      name: 'Analytics',
      href: '/analytics',
      icon: ChartBarIcon,
      roles: ['TENANT_ADMIN', 'SYSTEM_ADMIN', 'AGENT'],
    },
    {
      name: 'Notifications',
      href: '/notifications',
      icon: BellIcon,
      roles: ['TENANT_ADMIN', 'SYSTEM_ADMIN'],
    },
    {
      name: 'Settings',
      href: '/settings',
      icon: Cog6ToothIcon,
      roles: ['TENANT_ADMIN', 'SYSTEM_ADMIN'],
    },
  ];

  // Filter navigation items based on user roles
  const filteredNavigation = navigation.filter(item => {
    if (!item.roles) return true; // Show items without role restrictions
    return hasAnyRole(item.roles);
  });

  const isActive = (href: string) => {
    if (href === '/dashboard') {
      return location.pathname === '/' || location.pathname === '/dashboard';
    }
    return location.pathname.startsWith(href);
  };

  const SidebarContent = () => (
    <div className="flex flex-col h-full">
      {/* Logo */}
      <div className="flex items-center h-16 px-6 border-b border-gray-200">
        <div className="flex items-center">
          <div className="h-8 w-8 bg-primary-600 rounded-lg flex items-center justify-center">
            <svg
              className="h-5 w-5 text-white"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
              />
            </svg>
          </div>
          <span className="ml-3 text-xl font-semibold text-gray-900">
            Inticky
          </span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
        {filteredNavigation.map((item) => {
          const Icon = item.icon;
          const active = isActive(item.href);
          
          return (
            <NavLink
              key={item.name}
              to={item.href}
              className={cn(
                'group flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors',
                active
                  ? 'bg-primary-100 text-primary-900 border-r-2 border-primary-600'
                  : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
              )}
              onClick={() => onClose()}
            >
              <Icon
                className={cn(
                  'mr-3 h-5 w-5 flex-shrink-0',
                  active ? 'text-primary-600' : 'text-gray-400 group-hover:text-gray-500'
                )}
                aria-hidden="true"
              />
              <span className="flex-1">{item.name}</span>
              {item.badge && (
                <span className="ml-3 inline-block py-0.5 px-2 text-xs font-medium rounded-full bg-primary-100 text-primary-600">
                  {item.badge}
                </span>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* User info footer */}
      <div className="flex-shrink-0 border-t border-gray-200 p-4">
        <div className="flex items-center">
          <div className="flex-shrink-0">
            <div className="h-8 w-8 rounded-full bg-primary-100 flex items-center justify-center">
              <span className="text-sm font-medium text-primary-700">
                {/* User initials will be added here */}
                U
              </span>
            </div>
          </div>
          <div className="ml-3 flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-900 truncate">
              Current User
            </p>
            <p className="text-xs text-gray-500 truncate">
              {isSystemAdmin() && 'System Admin'}
              {!isSystemAdmin() && isTenantAdmin() && 'Tenant Admin'}
              {!isSystemAdmin() && !isTenantAdmin() && isAgent() && 'Agent'}
              {!isSystemAdmin() && !isTenantAdmin() && !isAgent() && 'User'}
            </p>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <>
      {/* Mobile sidebar */}
      <Transition.Root show={open} as={Fragment}>
        <Dialog as="div" className="relative z-50 lg:hidden" onClose={onClose}>
          <Transition.Child
            as={Fragment}
            enter="transition-opacity ease-linear duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="transition-opacity ease-linear duration-300"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="fixed inset-0 bg-gray-900/80" />
          </Transition.Child>

          <div className="fixed inset-0 flex">
            <Transition.Child
              as={Fragment}
              enter="transition ease-in-out duration-300 transform"
              enterFrom="-translate-x-full"
              enterTo="translate-x-0"
              leave="transition ease-in-out duration-300 transform"
              leaveFrom="translate-x-0"
              leaveTo="-translate-x-full"
            >
              <Dialog.Panel className="relative mr-16 flex w-full max-w-xs flex-1">
                <Transition.Child
                  as={Fragment}
                  enter="ease-in-out duration-300"
                  enterFrom="opacity-0"
                  enterTo="opacity-100"
                  leave="ease-in-out duration-300"
                  leaveFrom="opacity-100"
                  leaveTo="opacity-0"
                >
                  <div className="absolute left-full top-0 flex w-16 justify-center pt-5">
                    <button
                      type="button"
                      className="-m-2.5 p-2.5"
                      onClick={onClose}
                    >
                      <span className="sr-only">Close sidebar</span>
                      <XMarkIcon className="h-6 w-6 text-white" aria-hidden="true" />
                    </button>
                  </div>
                </Transition.Child>
                
                <div className="flex grow flex-col gap-y-5 overflow-y-auto bg-white">
                  <SidebarContent />
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </Dialog>
      </Transition.Root>

      {/* Desktop sidebar */}
      <div className="hidden lg:fixed lg:inset-y-0 lg:z-40 lg:flex lg:w-64 lg:flex-col">
        <div className="flex grow flex-col gap-y-5 overflow-y-auto bg-white border-r border-gray-200">
          <SidebarContent />
        </div>
      </div>
    </>
  );
};
