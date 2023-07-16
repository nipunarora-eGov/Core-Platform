import React from "react";
import { PhonelinkSetup } from "./PhonelinkSetup";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PhonelinkSetup",
  component: PhonelinkSetup,
};

export const Default = () => <PhonelinkSetup />;
export const Fill = () => <PhonelinkSetup fill="blue" />;
export const Size = () => <PhonelinkSetup height="50" width="50" />;
export const CustomStyle = () => <PhonelinkSetup style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PhonelinkSetup className="custom-class" />;