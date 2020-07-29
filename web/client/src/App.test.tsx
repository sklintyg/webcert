import React from 'react';
import {render} from '@testing-library/react';
import App from './App';
import {mount} from "enzyme";

test('renders app without crashing', () => {
  const {getByText} = render(<App />);
  const linkElement = getByText(/Hello react/i);
  expect(linkElement).toBeInTheDocument();
});

it('renders the app and heading', () => {
  const wrapper = mount(<App />);
  expect(wrapper.find('h1').text()).toBe('Hello React');
});

